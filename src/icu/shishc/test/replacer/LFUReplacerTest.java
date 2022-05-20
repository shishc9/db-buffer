package icu.shishc.test.replacer;

import icu.shishc.disk.Page;
import org.junit.Before;
import org.junit.Test;
import icu.shishc.replacer.FrameDescriptor;
import icu.shishc.replacer.LFUReplacer;

import java.util.HashMap;

public class LFUReplacerTest {

    private LFUReplacer<Integer, Page> lfuReplacer;
    private HashMap<Integer, FrameDescriptor> frameTable;

    @Before
    public void lfuInit() {
        // lfu 初始化 1 2 3
        lfuReplacer = new LFUReplacer<>(3);
        frameTable = new HashMap<>(3);
        byte[] b1 = new byte[1];
        b1[0] = 1;
        FrameDescriptor descriptor1 = new FrameDescriptor();
        descriptor1.setPageNum(1);
        frameTable.put(1, descriptor1);
        lfuReplacer.put(1, new Page(b1), frameTable);
        byte[] b2 = new byte[1];
        b2[0] = 2;
        FrameDescriptor descriptor2 = new FrameDescriptor();
        descriptor2.setPageNum(2);
        frameTable.put(2, descriptor2);
        lfuReplacer.put(2, new Page(b2), frameTable);
        byte[] b3 = new byte[1];
        b3[0] = 3;
        FrameDescriptor descriptor3 = new FrameDescriptor();
        descriptor3.setPageNum(3);
        frameTable.put(3, descriptor3);
        lfuReplacer.put(3, new Page(b3), frameTable);
        lfuReplacer.showLFUList();
        lfuReplacer.showFreqList();
        System.out.println("lfuMemorySize:" + lfuReplacer.getMemorySize());
        System.out.println("lfuMinFreq:" + lfuReplacer.getMinFreq());
        System.out.println("----------init finish.");
    }

    @Test
    public void getTest() {
        lfuReplacer.get(1);
        lfuReplacer.get(1);
        lfuReplacer.get(1);
        lfuReplacer.showFreqList();
        lfuReplacer.get(2);
        lfuReplacer.showFreqList();
        lfuReplacer.get(3);
        lfuReplacer.get(3);
        lfuReplacer.get(3);
        lfuReplacer.get(3);
        lfuReplacer.showFreqList();
    }

    @Test
    public void removeTest() {
        lfuReplacer.remove(4, frameTable);
        lfuReplacer.showFreqList();
        lfuReplacer.showLFUList();
        lfuReplacer.remove(1, frameTable);
        lfuReplacer.get(2);
        lfuReplacer.showFreqList();
        lfuReplacer.showLFUList();
        lfuReplacer.remove(3, frameTable);
        FrameDescriptor frameDescriptor = frameTable.get(2);
        frameDescriptor.setPinned(true);
        frameTable.put(2, frameDescriptor);
        lfuReplacer.remove(2, frameTable);
        lfuReplacer.showFreqList();
        lfuReplacer.showLFUList();
        System.out.println("minFreq: " + lfuReplacer.getMinFreq());
        FrameDescriptor descriptor = frameTable.get(2);
        descriptor.setPinned(false);
        frameTable.put(2, descriptor);
        lfuReplacer.remove(2, frameTable);
        lfuReplacer.showLFUList();
        System.out.println("minFreq: " + lfuReplacer.getMinFreq());
    }

    @Test
    public void putTest() {
        lfuReplacer.showLFUList();
        byte[] bytes0 = new byte[1];
        bytes0[0] = 100;
        System.out.println(lfuReplacer.put(1, new Page(bytes0), frameTable));
        lfuReplacer.showLFUList();
        byte[] bytes1 = new byte[1];
        bytes1[0] = 4;
        System.out.println(lfuReplacer.put(4, new Page(bytes1), frameTable));
        lfuReplacer.showLFUList();
        lfuReplacer.remove(2, frameTable);
        lfuReplacer.showLFUList();
        byte[] bytes2 = new byte[1];
        bytes2[0] = 5;
        System.out.println(lfuReplacer.put(5, new Page(bytes2), frameTable));
        System.out.println(lfuReplacer.getMemorySize());
        lfuReplacer.showLFUList();
        lfuReplacer.remove(1, frameTable);
        System.out.println("minFreq:" + lfuReplacer.getMinFreq());
        lfuReplacer.showLFUList();
    }

}
