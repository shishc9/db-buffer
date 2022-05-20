package icu.shishc.test.bufferpool;

import icu.shishc.bufferpool.BufferPoolInstance;
import icu.shishc.disk.DBFile;
import icu.shishc.disk.Page;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BufferPoolInstanceTest {

    private BufferPoolInstance bufferPoolInstance;
    private DBFile dbFile;
    private ArrayList<Integer> pageIdList;

//    @Before
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
     * icu.shishc.test pinPage(), unPinPage(), and whether a dirty page is written to icu.shishc.disk.
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
     * icu.shishc.test newPage(), freePage().
     */
    @Test
    public void test02() throws IOException {
        bufferPoolInstance.newPage(1, "new page 1");
        bufferPoolInstance.flushPages();
        bufferPoolInstance.showBufferPoolStatus();
        System.out.println("pageIdList:" + pageIdList);
        bufferPoolInstance.freePage(4);
    }

    @Test
    public void rateTest() throws IOException {
        ArrayList<BufferPoolInstance> singleListArr = new ArrayList<>();
        BufferPoolInstance singleListBPSize4 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("singleList4", 4).
                setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 4)
                .build();
        BufferPoolInstance singleListBPSize8 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("singleList8", 8).
                setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 8)
                .build();
        BufferPoolInstance singleListBPSize16 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("singleList16", 16).
                setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 16)
                .build();
        singleListArr.add(singleListBPSize4);
        singleListArr.add(singleListBPSize8);
        singleListArr.add(singleListBPSize16);

        ArrayList<BufferPoolInstance> lruArr = new ArrayList<>();
        BufferPoolInstance lruBPSize4 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lru4", 4).
                setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 4)
                .build();
        BufferPoolInstance lruBPSize8 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lru8", 8).
                setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 8)
                .build();
        BufferPoolInstance lruBPSize16 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lru16", 16).
                setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 16)
                .build();
        lruArr.add(lruBPSize4);
        lruArr.add(lruBPSize8);
        lruArr.add(lruBPSize16);

        ArrayList<BufferPoolInstance> lfuArr = new ArrayList<>();
        BufferPoolInstance lfuBPSize4 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lfu4", 4).
                setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 4)
                .build();
        BufferPoolInstance lfuBPSize8 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lfu8", 8).
                setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 8)
                .build();
        BufferPoolInstance lfuBPSize16 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("lfu16", 16).
                setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 16)
                .build();
        lfuArr.add(lfuBPSize4);
        lfuArr.add(lfuBPSize8);
        lfuArr.add(lfuBPSize16);

        ArrayList<BufferPoolInstance> cLruArr = new ArrayList<>();
        BufferPoolInstance concurrentLRUBPSize4 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("clru4", 4).
                setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 4)
                .build();
        BufferPoolInstance concurrentLRUBPSize8 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("clru8", 8).
                setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 8)
                .build();
        BufferPoolInstance concurrentLRUBPSize16 = new BufferPoolInstance.BufferPoolBuilder()
                .setDBFile("clru16", 16).
                setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 16)
                .build();
        cLruArr.add(concurrentLRUBPSize4);
        cLruArr.add(concurrentLRUBPSize8);
        cLruArr.add(concurrentLRUBPSize16);

        ArrayList<String> input4 = new ArrayList<>();
        input4.add("1");
        ArrayList<String> input8 = new ArrayList<>();
        ArrayList<String> input16 = new ArrayList<>();
        ArrayList<String> input = new ArrayList<>();
        String[] str = {
                "1", "2", "3", "5", "1", "6", "2", "5", "4", "9", "17", "16", "15", "13", "13", "56", "234", "11", "17", "12",
                "34", "3", "5", "6", "45", "4532", "321", "2", "43", "42", "54", "21", "3", "5", "6", "123", "54", "123", "123", "21"
        };

        for (int i = 0;i < str.length; i++) {
//            singleListBPSize4.pinPage(Integer.parseInt(str[i]), true , str[i]);
//            singleListBPSize8.pinPage(Integer.parseInt(str[i]), true , str[i]);
//            singleListBPSize16.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lruBPSize4.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lruBPSize8.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lruBPSize16.pinPage(Integer.parseInt(str[i]), true , str[i]);
            concurrentLRUBPSize4.pinPage(Integer.parseInt(str[i]), true , str[i]);
            concurrentLRUBPSize8.pinPage(Integer.parseInt(str[i]), true , str[i]);
            concurrentLRUBPSize16.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lfuBPSize4.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lfuBPSize8.pinPage(Integer.parseInt(str[i]), true , str[i]);
            lfuBPSize16.pinPage(Integer.parseInt(str[i]), true , str[i]);
        }

//        System.out.println(singleListBPSize4.getHitRate());
//        System.out.println(singleListBPSize8.getHitRate());
//        System.out.println(singleListBPSize16.getHitRate());
        System.out.println(lruBPSize4.getHitRate());
        System.out.println(lruBPSize8.getHitRate());
        System.out.println(lruBPSize16.getHitRate());
        System.out.println(concurrentLRUBPSize4.getHitRate());
        System.out.println(concurrentLRUBPSize8.getHitRate());
        System.out.println(concurrentLRUBPSize16.getHitRate());
        System.out.println(lfuBPSize4.getHitRate());
        System.out.println(lfuBPSize8.getHitRate());
        System.out.println(lfuBPSize16.getHitRate());


    }



}
