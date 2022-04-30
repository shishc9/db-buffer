package test.bufferpool;

import bufferpool.BufferPoolInstance;
import disk.DBFile;
import disk.Page;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BufferPoolInstanceTest {

    private BufferPoolInstance bufferPoolInstance;
    private DBFile dbFile;
    private ArrayList<Integer> pageIdList;

    @Before
    public void bufferInit() throws IOException {
        dbFile = new DBFile("buffer", 5);
        pageIdList = new ArrayList<>();
        bufferPoolInstance = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("buffer", 5)
                .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 3)
                .build();

        String[] dataStrArr = {
                "I have had my invitation to this world's festival, and thus my life has been blessed.",
                "Early in the day it was whispered that we should sail in a boat, only thou and I, and never a soul in the world would know of this our pilgrimage to no country and to no end.",
                "In the meanwhile I smile and I sing all alone. In the meanwhile the air is filling with the perfume of promise.",
                "When grace is lost from life, come with a burst of song.",
                "Walking down Bristol Street, the crowds upon the pavement were fields of harvest wheat."
        };

        for (int i = 0; i < dataStrArr.length - 1; i++) {
            Integer pageId = dbFile.allocatePages(1);
            dbFile.writePage(pageId, new Page(dataStrArr[i].getBytes(StandardCharsets.UTF_8)));
            pageIdList.add(pageId);
        }

        System.out.println("page write finished.");
        System.out.println("pageIdList:" + pageIdList);
        System.out.println("--------init finished.----------");
        bufferPoolInstance.showBufferPoolStatus();
    }

    /**
     * test pinPage(), unPinPage(), and whether a dirty page is written to disk.
     */
    @Test
    public void test01() throws IOException {
        bufferPoolInstance.pinPage(1, false, null);
        bufferPoolInstance.pinPage(2, false, null);
        bufferPoolInstance.pinPage(3, false, null);
        bufferPoolInstance.pinPage(1, false, null);
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.unPinPage(3);
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.pinPage(2, false, "flush or not");
        bufferPoolInstance.showBufferPoolStatus();
        bufferPoolInstance.flushPage(2);
        bufferPoolInstance.pinPage(4, true, "page 4");
        bufferPoolInstance.showBufferPoolStatus();
    }

    /**
     * test newPage(), freePage().
     */
    @Test
    public void test02() throws IOException {
        bufferPoolInstance.newPage(1, "new page 1");
        bufferPoolInstance.flushPages();
        bufferPoolInstance.showBufferPoolStatus();
        System.out.println("pageIdList:" + pageIdList);
        bufferPoolInstance.freePage(4);
    }

}
