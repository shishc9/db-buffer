package icu.shishc.test.bufferpool;

import icu.shishc.bufferpool.BufferPoolInstance;
import icu.shishc.disk.DBFile;
import icu.shishc.disk.Page;
import icu.shishc.interfac3.Pair;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * 缓冲池功能性测试
 */
public class BufferPoolFuncTest {

    private DBFile dbFile;
    private BufferPoolInstance bufferPoolInstance;
    private ArrayList<Integer> pageIdList;

    @Before
    public void bufferInit() throws IOException {
        System.out.println("-----init start-----");
        dbFile = new DBFile("bp-func-icu.shishc.test", 10);
        pageIdList = new ArrayList<>();
        bufferPoolInstance = new BufferPoolInstance.BufferPoolBuilder()
                .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 3)
                .setDBFile("bp-func-icu.shishc.test", 10)
                .build();

        String[] initData = {
                "1", "2", "3", "4", "5"
        };

        for (String initDatum : initData) {
            Integer pageId = dbFile.allocatePages(1);
            dbFile.writePage(pageId, new Page(initDatum.getBytes(StandardCharsets.UTF_8)));
            pageIdList.add(pageId);
        }
        System.out.println("pageIdList:" + pageIdList);
        System.out.println("-----init finish-----");
    }

    /**
     * icu.shishc.test pinPage(), flushPage().
     */
    @Test
    public void test01() throws IOException {
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.pinPage(1, false, null);
        bufferPoolInstance.pinPage(2, false, null);
        bufferPoolInstance.pinPage(3, false, null);
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.pinPage(1, false, null);
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.pinPage(4, false, null);
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.pinPage(3, true, "***");
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.flushPage(3);
        bufferPoolInstance.showBufferPoolStatus();
    }

    /**
     * icu.shishc.test newPage(), unPinPage().
     */
    @Test
    public void test02() throws IOException {
        Pair<Integer, Page> pair = bufferPoolInstance.newPage(1, "new");
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.unPinPage(pair.first);
        bufferPoolInstance.showBufferPoolStatus();
    }
}
