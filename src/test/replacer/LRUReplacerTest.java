package test.replacer;

import disk.Page;
import org.junit.Before;
import org.junit.Test;
import replacer.FrameDescriptor;
import replacer.LRUReplacer;

import java.util.HashMap;
import java.util.Map;

/**
 * replacer.LRUReplacer 测试类.
 */
public class LRUReplacerTest {

    private LRUReplacer<Integer, Page> lruReplacer;
    private HashMap<Integer, FrameDescriptor> frameTable;

    @Before
    public void lruInit() {
        lruReplacer = new LRUReplacer<>(3);
        frameTable = new HashMap<>(3);
        System.out.println("LRUReplacer init...\n" +
                "maxCapacity:" + lruReplacer.getMaxMemorySize() + "\n" +
                "curSize:" + lruReplacer.getMemorySize() + "\n" +
                "LRUReplacer init end...");

        FrameDescriptor frameDescriptor1 = new FrameDescriptor();
        frameDescriptor1.setPageNum(1);
        frameTable.put(1, frameDescriptor1);
        FrameDescriptor frameDescriptor2 = new FrameDescriptor();
        frameDescriptor2.setPageNum(2);
        frameTable.put(2, frameDescriptor2);
        FrameDescriptor frameDescriptor3 = new FrameDescriptor();
        frameDescriptor3.setPageNum(3);
        frameTable.put(3, frameDescriptor3);
        System.out.println("-----showFrameTable-----");
        showFrameTable();

        byte[] bytes1 = new byte[1];
        bytes1[0] = 1;
        lruReplacer.put(1, new Page(bytes1), frameTable);
        byte[] bytes2 = new byte[1];
        bytes2[0] = 2;
        lruReplacer.put(2, new Page(bytes2), frameTable);
        byte[] bytes3 = new byte[1];
        bytes3[0] = 3;
        lruReplacer.put(3, new Page(bytes3), frameTable);
        System.out.println("-----showLRUList-----");
        lruReplacer.showLRUList();
    }

    @Test
    public void getKeyTest() {
        System.out.println("get1:" + lruReplacer.get(1));
        lruReplacer.showLRUList();
        System.out.println("get2:" + lruReplacer.get(2));
        lruReplacer.showLRUList();
        System.out.println("get4:" + lruReplacer.get(4));
        lruReplacer.showLRUList();
    }

    @Test
    public void removeTest() {
        System.out.println("remove5:" + lruReplacer.remove(5, frameTable));
        FrameDescriptor frameDescriptor = frameTable.get(1);
        frameDescriptor.setPinned(true);
        frameTable.put(1, frameDescriptor);
        System.out.println("remove1 with pin:" + lruReplacer.remove(1, frameTable));
        System.out.println("remove2 without pin:" + lruReplacer.remove(2, frameTable));
        lruReplacer.showLRUList();
        showFrameTable();
    }

    @Test
    public void getWithoutMoveTest() {
        System.out.println("get1:" + lruReplacer.getWithoutMove(1));
        lruReplacer.showLRUList();
        System.out.println("get2:" + lruReplacer.getWithoutMove(2));
        lruReplacer.showLRUList();
        System.out.println("get4:" + lruReplacer.getWithoutMove(4));
        lruReplacer.showLRUList();
    }

    @Test
    public void putTest() {
        lruReplacer.showLRUList();
        byte[] bytes = new byte[1];
        bytes[0] = 100;
        Page page = new Page(bytes);
        lruReplacer.put(1, page, frameTable);
        lruReplacer.showLRUList();
        FrameDescriptor frameDescriptor = frameTable.get(2);
        frameDescriptor.setPinned(true);
        byte[] bytes1 = new byte[1];
        bytes1[0] = 101;
        frameTable.put(2, frameDescriptor);
        showFrameTable();
        System.out.println(lruReplacer.put(4, new Page(bytes1), frameTable));
        lruReplacer.showLRUList();
    }

    private void showFrameTable() {
        for (Map.Entry<Integer, FrameDescriptor> entry : frameTable.entrySet()) {
            System.out.println("frameId:" + entry.getKey() + ", pageId:" + entry.getValue().getPageNum());
        }
    }

}
