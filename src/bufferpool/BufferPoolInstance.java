package bufferpool;

import disk.DBFile;
import disk.Page;
import exception.FramePoolConsistencyException;
import exception.IllegalPageNumberException;
import exception.PageNotPinnedException;
import exception.PagePinnedException;
import interfac3.Pair;
import interfac3.PutVO;
import interfac3.Replacer;
import replacer.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BufferPoolInstance {

    private static final Integer REPLACER_SIZE = 16;
    private static final Integer PAGE_NUM_SIZE = 32;
    private static final String DEFAULT_DB_FILE_NAME = "example";

    // 维护缓冲池中每一页的状态.
    private HashMap<Integer, FrameDescriptor> frameTable;
    private Replacer<Integer, Page> replacer;
    private DBFile dbFile;

    // 支持的缓冲替换策略枚举类.
    public enum ReplacerEnum {
        LRUReplacer,
        LFUReplacer,
        SingleListLRUReplacer,
        ConcurrentLRUReplacer
    }

    /**
     * 提供给外界的buffer pool builder. => 采用构建者设计模式.
     * eg. new BufferPool.BufferPoolBuilder.build();
     * 该类不提供默认选项.
     */
    public static class BufferPoolBuilder {

        private BufferPoolInstance bufferPoolInstance;

        public BufferPoolBuilder() throws IOException {
            bufferPoolInstance = new BufferPoolInstance();
        }

        public BufferPoolBuilder setReplacer(ReplacerEnum replacerName, Integer initSize) {
            switch (replacerName) {
                case LFUReplacer: {
                    bufferPoolInstance.replacer = new LFUReplacer<>(initSize);
                    break;
                }
                case LRUReplacer: {
                    bufferPoolInstance.replacer = new LRUReplacer<>(initSize);
                    break;
                }
                case SingleListLRUReplacer: {
                    bufferPoolInstance.replacer = new SingleListLRUReplacer<>(initSize);
                    break;
                }
                case ConcurrentLRUReplacer: {
                    bufferPoolInstance.replacer = new ConcurrentLRUReplacer<>(initSize);
                    break;
                }
            }
            bufferPoolInstance.frameTable = new HashMap<>(initSize);
            return this;
        }

        public BufferPoolBuilder setDBFile(String name, Integer pageNums) throws IOException {
            bufferPoolInstance.dbFile = new DBFile(name, pageNums);
            return this;
        }

        public BufferPoolInstance build() {
            return bufferPoolInstance;
        }
    }

    public Replacer<Integer, Page> getReplacer() {
        return this.replacer;
    }

    public DBFile getDbFile() {
        return this.dbFile;
    }

    /**
     * new一个pageNum大小的数据页，返回数据页号和page，并将page固定.
     * @param pageNum 数据页大小
     * @return 页号&page
     */
    public Pair<Integer, Page> newPage(int pageNum, String data) throws IOException {
        // 如果缓存已满，应该进行页面替换.
        if (frameTable.size() == replacer.getMaxMemorySize()) {
            return null;
        }
        Integer pageId = dbFile.allocatePages(pageNum);
        Page page = pinPage(pageId, false, data);
        Pair<Integer, Page> pair = new Pair<>(pageId, page);
        return pair;
    }

    /**
     * 删除一个page.
     * @param pageId 页号
     */
    public void freePage(int pageId) throws IOException {
        // 在缓存中.
        FrameDescriptor descriptor = frameTable.get(pageId);
        // 当前删除页面正在被访问，则抛出异常.
        if (descriptor.isPinned() || descriptor.getPinCount().intValue() > 0) {
            throw new PagePinnedException();
        }
        // 删除页面.
        dbFile.deallocatePages(pageId, 1);
    }

    /**
     * 刷新某个页面.
     * 如果这个页面在缓存池中且状态为脏页，则进行刷新.
     * @param pageNum 要进行刷新的页面ID.
     */
    public void flushPage(int pageNum) throws IOException {
        // 参数校验
        if (pageNum < 0 || pageNum > dbFile.getNumPages()) {
            throw new IllegalPageNumberException();
        }
        // 判断这个页面在不在缓存池中. 如果不在什么都不做.
        Page page = replacer.getWithoutMove(pageNum);
        if (page != null) {
            // 获取页面对应状态
            FrameDescriptor frameDescriptor = frameTable.get(pageNum);
            // 进行页面核对且必须为脏页.
            if (frameDescriptor.getPageNum() == pageNum && frameDescriptor.isDirty()) {
                dbFile.writePage(pageNum, page);
                frameDescriptor.setDirty(false);
                frameTable.put(pageNum, frameDescriptor);
            }
            System.out.println("flush page number:" + pageNum);
        }
    }

    public void flushPage(int pageNum, Page page) throws IOException {
        // 参数校验
        if (pageNum < 0 || pageNum > dbFile.getNumPages()) {
            throw new IllegalPageNumberException();
        }
        // 判断这个页面在不在缓存池中. 如果不在什么都不做.
        if (page != null) {
            // 获取页面对应状态
            FrameDescriptor frameDescriptor = frameTable.get(pageNum);
            // 进行页面核对且必须为脏页.
            if (frameDescriptor.getPageNum() == pageNum && frameDescriptor.isDirty()) {
                dbFile.writePage(pageNum, page);
                frameDescriptor.setDirty(false);
                frameTable.put(pageNum, frameDescriptor);
            }
            System.out.println("flush page number:" + pageNum);
        }
    }

    /**
     * 刷新所有脏页，遍历frameTable，若是脏页则通过pageId在replacer中获取Page，后刷新到数据库中.
     * 刷新完成更新frameTable状态.
     */
    public void flushPages() throws IOException {
        System.out.println("flush all pages.");
        for(Map.Entry<Integer, FrameDescriptor> entry : frameTable.entrySet()) {
            // entry合法，pageId对应页为脏页且该页存在于缓存池中.
            if (entry != null && entry.getValue().isDirty() && replacer.contains(entry.getKey())) {
                dbFile.writePage(entry.getKey(), replacer.getWithoutMove(entry.getKey()));
                entry.getValue().setDirty(false);
            }
        }
    }

    public Page pinPage(int pinPageId, boolean empty, String data) throws IOException {
        Page curPage;
        if (checkInPoolOrNot(pinPageId)) {
            frameTable.get(pinPageId).increasePinCount();
            if (data != null) {
                frameTable.get(pinPageId).setDirty(true);
                replacer.put(pinPageId, new Page(data.getBytes(StandardCharsets.UTF_8)), frameTable);
            }
            return replacer.get(pinPageId);
        }
        if (frameTable.size() == replacer.getMemorySize() && Objects.equals(replacer.getMemorySize(), replacer.getMaxMemorySize())) {
            // 这个id应该由replacer来决定，将frameTable状态传入replacer中，返回待删除的pageId.
            Integer pageIdReplace = -1;
            curPage = new Page();

            if (!empty) {
                dbFile.readPage(pinPageId, curPage);
            } else {
                curPage.data = data.getBytes(StandardCharsets.UTF_8);
            }

            PutVO putVO = replacer.put(pinPageId, curPage, frameTable);
            pageIdReplace = (Integer) putVO.getKey();


            if (pageIdReplace != null) {
                frameTable.get(pageIdReplace).setDirty(true);
                flushPage(pageIdReplace, (Page) putVO.getValue());
            }

            FrameDescriptor descriptor = new FrameDescriptor();
            descriptor.setPageNum(pinPageId);
            frameTable.remove(pageIdReplace);
            frameTable.put(pinPageId, descriptor);
            return curPage;
        }

        curPage = new Page();
        if (!empty) {
            dbFile.readPage(pinPageId, curPage);
        } else {
            curPage.data = data.getBytes(StandardCharsets.UTF_8);
        }

        PutVO putVO = replacer.put(pinPageId, curPage, frameTable);
        if (putVO.getKey() == null && putVO.getMsg().equals("ADD_NODE")) {
            FrameDescriptor descriptor = new FrameDescriptor();
            descriptor.setPageNum(pinPageId);
            frameTable.put(pinPageId, descriptor);
        } else if (putVO.getKey() == null && putVO.getMsg().equals("KEY_IN_POOL")) {
            FrameDescriptor descriptor = frameTable.get(pinPageId);
            descriptor.setDirty(true);
            frameTable.put(pinPageId, descriptor);
        } else if (putVO.getKey() != null) {
            frameTable.remove(putVO.getKey(), putVO.getValue());
            FrameDescriptor descriptor = new FrameDescriptor();
            descriptor.setPageNum(pinPageId);
            frameTable.put(pinPageId, descriptor);
            System.out.println("frameTable remove key:" + putVO.getKey());
        }

        return curPage;
    }

    /**
     * @param unPinPageId
     */
    public void unPinPage(int unPinPageId) throws IOException {
        FrameDescriptor descriptor = frameTable.get(unPinPageId);
        if (descriptor != null) {
            if (descriptor.getPinCount().intValue() > 0) {
                descriptor.decreasePinCount();
                frameTable.put(unPinPageId, descriptor);
            } else if (descriptor.getPinCount().intValue() == 0) {
                descriptor.setPinned(false);
                descriptor.setDirty(true);
                frameTable.put(unPinPageId, descriptor);
                flushPage(unPinPageId);
            } else {
                throw new PageNotPinnedException();
            }
        }
    }

    /**
     * 判断该页面是否在缓冲池中.
     * @param pageId 要查找的页面
     * @return t/f
     * @throws FramePoolConsistencyException buffer frame table 和 replacer 状态不一致异常.
     */
    private boolean checkInPoolOrNot(Integer pageId) throws FramePoolConsistencyException {
        Page page = replacer.getWithoutMove(pageId);
        if (page == null && !frameTable.containsKey(pageId)) {
            return false;
        } else if (page != null && frameTable.containsKey(pageId)) {
            // 缓冲池有该页面，frameTable有该页面，且frameTable的pageId正常.
            if (!Objects.equals(frameTable.get(pageId).getPageNum(), pageId)) {
                throw new FramePoolConsistencyException();
            }
            return true;
        }
        return false;
    }

    public void showBufferPoolStatus() {
        System.out.println("show buffer pool status: ------");
        replacer.showReplacerStatus();
        for (Map.Entry<Integer, FrameDescriptor> entry : frameTable.entrySet()) {
            System.out.println("key:" + entry.getKey() + ", frameDescriptor:" + entry.getValue());
        }
        System.out.println("buffer pool status end. -------");
    }
}
