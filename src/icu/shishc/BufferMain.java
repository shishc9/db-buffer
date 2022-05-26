package icu.shishc;

import icu.shishc.bufferpool.BufferPoolInstance;
import icu.shishc.replacer.ConcurrentLRUReplacer;
import icu.shishc.replacer.LRUReplacer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0, time = 1)
@Measurement(iterations = 1, time = 1)
@Fork(1)
@Threads(1)
public class BufferMain {

    private final static int KB = 1024;

    private final ArrayList<BufferPoolInstance> lruList = new ArrayList<>();
    private final ArrayList<BufferPoolInstance> lfuList = new ArrayList<>();
    private final ArrayList<BufferPoolInstance> concurrentLRUList = new ArrayList<>();

    private final ArrayList<String> data = new ArrayList<>();

//    /**
//     * test06 test05 setup
//     */
//    @Setup
//    public void setup03() throws IOException {
//        String csvFile = "UserBehavior.csv";
//        String csvSplit = ",";
//        String line;
//
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
//        int ite = 1000000;
//        while (ite > 0 && (line = bufferedReader.readLine()) != null) {
//            String[] arr = line.split(csvSplit);
//            if (arr[3].equals("pv")) {
//                data.add(arr[1]);
//                ite --;
//            }
//        }
//
//        System.out.println(data.size());
//    }
//
//    /**
//     * 测试lru并发环境吞吐量
//     */
//    @Benchmark
//    public void test05() {
//        LRUReplacer<Integer, String> lruReplacer = new LRUReplacer<>(1024*KB);
//        for (int i = 0;i < data.size();i ++) {
//            String dataPage = data.get(i);
//            lruReplacer.put(Integer.parseInt(dataPage), dataPage, null);
//        }
//    }
//
//    /**
//     * 测试conlru并发环境吞吐量
//     */
//    @Benchmark
//    public void test06() {
//        ConcurrentLRUReplacer<Integer, String> concurrentLRUReplacer = new ConcurrentLRUReplacer<>(8, 1024*KB);
//        for (int i = 0;i < data.size();i ++) {
//            String dataPage = data.get(i);
//            concurrentLRUReplacer.put(Integer.parseInt(dataPage), dataPage, null);
//        }
//    }



    @Setup
    public void setup() throws IOException {
        String csvFile = "UserBehavior.csv";
        String csvSplit = ",";
        String line;

        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
        int ite = 30000;
        while (ite > 0 && (line = bufferedReader.readLine()) != null) {
            String[] arr = line.split(csvSplit);
            if (arr[3].equals("pv")) {
                data.add(arr[1]);
                ite --;
            }
        }

        System.out.println(data.size());

        int[] size = new int[]{4, 16, 64, 128, 256, 512, 1024, 2048, 4096};
        // 4k  16k  64k 128K 256k 512k 1m 2m 4m
        for (int i = 1;i < 10;i ++) {
            lruList.add(new BufferPoolInstance.BufferPoolBuilder()
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, size[i-1] * KB, null)
                    .build());
            lfuList.add(new BufferPoolInstance.BufferPoolBuilder()
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, size[i-1] * KB, null)
                    .build());
            concurrentLRUList.add(new BufferPoolInstance.BufferPoolBuilder()
                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, size[i-1] * KB, null)
                    .build());
        }
    }

    /**
     * 测试concurrentLRU并发函数 不同并发度 test04
     */
//    @Setup
//    public void setup02() throws IOException {
//        String csvFile = "UserBehavior.csv";
//        String csvSplit = ",";
//        String line;
//
//        BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
//        int ite = 30000000;
//        while (ite > 0 && (line = bufferedReader.readLine()) != null) {
//            String[] arr = line.split(csvSplit);
//            if (arr[3].equals("pv")) {
//                data.add(arr[1]);
//                ite --;
//            }
//        }
//
//        System.out.println(data.size());
//        int[] segNum = new int[]{2, 4, 8, 12, 16};
//        for (int i = 0; i < segNum.length; i++) {
//            concurrentLRUList.add(new BufferPoolInstance.BufferPoolBuilder()
//                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 1024*KB, segNum[i])
//                    .build());
//        }
//    }

    /**
     * 对于相同替换算法 不同缓冲池大小的测试
     */
    @Benchmark
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
    }

    @Benchmark
    public void test02() throws IOException {
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
    }

    @Benchmark
    public void test03() throws IOException {
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
    }

//    @Benchmark
//    public void test04() throws IOException {
//        System.out.println("=====con dif concurrency=====");
//        for (BufferPoolInstance bp : concurrentLRUList) {
//            long start = System.currentTimeMillis();
//            for (int i = 0; i < data.size();i ++) {
//                String dataPage = data.get(i);
//                bp.pinPage(Integer.parseInt(dataPage), true, dataPage);
//            }
//            System.out.println("cost time=>" + (System.currentTimeMillis() - start));
//            System.out.println("hit times=>" + bp.getReplacer().getHitCounts() + ", ioCounts=>" + bp.getHitCount());
//            System.out.println("bpSize=>" + bp.getBufferPoolSize() + ", rate=>" + bp.getHitRate());
//        }
//    }

    public static void main(String[] args) throws Exception {
        Options opts = new OptionsBuilder()
                .include(BufferMain.class.getSimpleName())
                .resultFormat(ResultFormatType.JSON)
                .build();
        new Runner(opts).run();
    }
}
