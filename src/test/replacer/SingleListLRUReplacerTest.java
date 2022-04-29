package test.replacer;

import disk.Page;
import org.junit.Before;
import org.junit.Test;
import replacer.FrameDescriptor;
import replacer.SingleListLRUReplacer;

import java.util.HashMap;

public class SingleListLRUReplacerTest {

    private SingleListLRUReplacer<Integer, Page> singleListLRUReplacer;
    private HashMap<Integer, FrameDescriptor> frameTable;

    @Before
    public void singleListLRUInit() {
        singleListLRUReplacer = new SingleListLRUReplacer<>(3);
        frameTable = new HashMap<>(3);

        byte[] bytes1 = new byte[1];
        bytes1[0] = 1;
        FrameDescriptor frameDescriptor1 = new FrameDescriptor();
        frameDescriptor1.setPageNum(1);
        frameTable.put(1, frameDescriptor1);
        singleListLRUReplacer.put(1, new Page(bytes1), frameTable);

        byte[] bytes2 = new byte[1];
        bytes2[0] = 2;
        FrameDescriptor frameDescriptor2 = new FrameDescriptor();
        frameDescriptor2.setPageNum(2);
        frameTable.put(2, frameDescriptor2);
        singleListLRUReplacer.put(2, new Page(bytes2), frameTable);

        byte[] bytes3 = new byte[1];
        bytes3[0] = 3;
        FrameDescriptor frameDescriptor3 = new FrameDescriptor();
        frameDescriptor3.setPageNum(1);
        frameTable.put(3, frameDescriptor3);
        singleListLRUReplacer.put(3, new Page(bytes3), frameTable);
        singleListLRUReplacer.showLRUList();
        System.out.println("------init finish.");
    }

    @Test
    public void getTest() {
        singleListLRUReplacer.get(2);
        singleListLRUReplacer.showLRUList();
        singleListLRUReplacer.get(1);
        singleListLRUReplacer.showLRUList();
        singleListLRUReplacer.get(5);
    }

    @Test
    public void removeTest() {
        singleListLRUReplacer.remove(1, frameTable);
        singleListLRUReplacer.showLRUList();
        byte[] bytes1 = new byte[1];
        bytes1[0] = 4;
        FrameDescriptor frameDescriptor1 = new FrameDescriptor();
        frameDescriptor1.setPageNum(4);
        frameDescriptor1.setPinned(true);
        frameTable.put(4, frameDescriptor1);
        singleListLRUReplacer.put(4, new Page(bytes1), frameTable);
        singleListLRUReplacer.showLRUList();
        singleListLRUReplacer.remove(4, frameTable);
        singleListLRUReplacer.showLRUList();
    }

    @Test
    public void putTest() {
        singleListLRUReplacer.showLRUList();
        byte[] bytes1 = new byte[1];
        bytes1[0] = 100;
        singleListLRUReplacer.put(1, new Page(bytes1), frameTable);
        singleListLRUReplacer.showLRUList();
        byte[] bytes2 = new byte[1];
        bytes2[0] = 4;
        FrameDescriptor frameDescriptor2 = new FrameDescriptor();
        frameDescriptor2.setPageNum(4);
        frameTable.put(4, frameDescriptor2);
        FrameDescriptor frameDescriptor = frameTable.get(3);
        frameDescriptor.setPinned(true);
        frameTable.put(3, frameDescriptor);
        singleListLRUReplacer.put(4, new Page(bytes2), frameTable);
        singleListLRUReplacer.showLRUList();
        singleListLRUReplacer.remove(3, frameTable);
        singleListLRUReplacer.showLRUList();
        byte[] bytes3 = new byte[1];
        bytes3[0] = 5;
        FrameDescriptor frameDescriptor3 = new FrameDescriptor();
        frameDescriptor3.setPageNum(5);
        frameTable.put(5, frameDescriptor3);
        singleListLRUReplacer.put(5, new Page(bytes3), frameTable);
        singleListLRUReplacer.showLRUList();
    }

}
