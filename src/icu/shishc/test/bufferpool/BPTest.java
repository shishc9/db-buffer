package icu.shishc.test.bufferpool;

import icu.shishc.bufferpool.BufferPoolInstance;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BPTest {

    private final static int KB = 1024;

    private ArrayList<BufferPoolInstance> lruList = new ArrayList<>();
    private ArrayList<BufferPoolInstance> lfuList = new ArrayList<>();
    private ArrayList<BufferPoolInstance> concurrentLRUList = new ArrayList<>();
    private ArrayList<BufferPoolInstance> singleList = new ArrayList<>();

    private final ArrayList<String> data = new ArrayList<>();

    @Before
    public void setup() throws IOException {
        String csvFile = "UserBehavior.csv";
        String csvSplit = ",";
        String line;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
        int ite = 5000000;
        while (ite > 0 && (line = bufferedReader.readLine()) != null) {
            String[] arr = line.split(csvSplit);
            if (arr[3].equals("pv")) {
                data.add(arr[1]);
                ite --;
            }
        }

        System.out.println(data.size());

        int[] size = new int[]{4, 16, 64, 128, 256, 512, 1024, 2048, 4096};
        // 4k 16k 64k 256k 512k 1m 2m
        for (int i = 1;i < 10;i ++) {
            lruList.add(new BufferPoolInstance.BufferPoolBuilder()
                            .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, size[i-1] * KB)
                            .build());
            lfuList.add(new BufferPoolInstance.BufferPoolBuilder()
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, size[i-1] * KB)
                    .build());
            concurrentLRUList.add(new BufferPoolInstance.BufferPoolBuilder()
                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, size[i-1] * KB)
                    .build());
            if (i < 5) {
                singleList.add(new BufferPoolInstance.BufferPoolBuilder()
                        .setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, size[i-1] * KB)
                        .build());
            }
        }
    }

    /**
     * 对于相同替换算法 不同缓冲池大小的测试
     */
    @Test
    public void test01() throws Exception {
        System.out.println("=====lru=====");
        for (BufferPoolInstance bp : lruList) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < data.size();i ++) {
                String dataPage = data.get(i);
                bp.pinPage(Integer.parseInt(dataPage), true, dataPage);
            }
            System.out.println("cost time=>" + (System.currentTimeMillis() - start));
            System.out.println("hit times=>" + bp.getReplacer().getHitCounts() + ", ioCounts=>" + bp.getHitCount());
            System.out.println("bpSize=>" + bp.getBufferPoolSize() + ", rate=>" + bp.getHitRate());
        }
        System.out.println("=====con=====");
        for (BufferPoolInstance bp : concurrentLRUList) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < data.size(); i++) {
                String dataPage = data.get(i);
                bp.pinPage(Integer.parseInt(dataPage), true, dataPage);
            }
            System.out.println("cost time=>" + (System.currentTimeMillis() - start));
            System.out.println("hit times=>" + bp.getReplacer().getHitCounts() + ", ioCounts=>" + bp.getHitCount());
            System.out.println("bpSize=>" + bp.getBufferPoolSize() + ", rate=>" + bp.getHitRate());
        }
        System.out.println("=====lfu=====");
        for (BufferPoolInstance bp : lfuList) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < data.size();i ++) {
                String dataPage = data.get(i);
                bp.pinPage(Integer.parseInt(dataPage), true, dataPage);
            }
            System.out.println("cost time=>" + (System.currentTimeMillis() - start));
            System.out.println("hit times=>" + bp.getReplacer().getHitCounts() + ", ioCounts=>" + bp.getHitCount());
            System.out.println("bpSize=>" + bp.getBufferPoolSize() + ", rate=>" + bp.getHitRate());
        }
//        System.out.println("=====single=====");
//        for (BufferPoolInstance bp : singleList) {
//            long start = System.currentTimeMillis();
//
//            for (int i = 0; i < data.size();i ++) {
//                String dataPage = data.get(i);
//                bp.pinPage(Integer.parseInt(dataPage), true, dataPage);
//            }
//            System.out.println("cost time=>" + (System.currentTimeMillis() - start));
//            System.out.println("hit times=>" + bp.getReplacer().getHitCounts() + ", ioCounts=>" + bp.getHitCount());
//            System.out.println("bpSize=>" + bp.getBufferPoolSize() + ", rate=>" + bp.getHitRate());
//        }
    }

    /**
     * 对于相同大小 不同替换算法的测试.
     */
    @Test
    public void test02() {

    }

}
