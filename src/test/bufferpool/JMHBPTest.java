package test.bufferpool;

import bufferpool.BufferPoolInstance;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(2)
public class JMHBPTest {

    public static ArrayList<String> data = new ArrayList<>();
    public static BufferPoolInstance lruBufferPoolInstance1024;
    public static BufferPoolInstance lruBufferPoolInstance4096;
    public static BufferPoolInstance lruBufferPoolInstance8192;
    public static BufferPoolInstance lfuBufferPoolInstance1024;
    public static BufferPoolInstance lfuBufferPoolInstance4096;
    public static BufferPoolInstance lfuBufferPoolInstance8192;
    public static BufferPoolInstance singleList1024;
    public static BufferPoolInstance singleList4096;
    public static BufferPoolInstance singleList8192;
    public static BufferPoolInstance concurrentLRU1024;
    public static BufferPoolInstance concurrentLRU4096;
    public static BufferPoolInstance concurrentLRU8192;



    static  {
        String csvFile = "UserBehavior.csv";
        String csvSplit = ",";
        String line;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(csvFile));
            int ite = 100000;
            while (ite > 0 && (line = bufferedReader.readLine()) != null) {
                ite --;
                String[] arr = line.split(csvSplit);
                if (arr[3].equals("pv")) {
                    data.add(arr[1]);
                }
            }

            lruBufferPoolInstance1024 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 10240)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 10240)
                    .build();
            lruBufferPoolInstance4096 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 40960)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 40960)
                    .build();
            lruBufferPoolInstance8192 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 81920)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LRUReplacer, 81920)
                    .build();
            lfuBufferPoolInstance1024 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 10240)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 10240)
                    .build();
            lfuBufferPoolInstance4096 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 40960)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 40960)
                    .build();
            lfuBufferPoolInstance8192 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 81920)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.LFUReplacer, 81920)
                    .build();
            singleList1024 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 10240)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 10240)
                    .build();
            singleList4096 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 40960)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 40960)
                    .build();
            singleList8192 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 81920)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 81920)
                    .build();
            concurrentLRU1024 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 10240)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 10240)
                    .build();
            concurrentLRU4096 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 40960)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 40960)
                    .build();
            concurrentLRU8192 = new BufferPoolInstance.BufferPoolBuilder()
//                    .setDBFile("'", 81920)
                    .setReplacer(BufferPoolInstance.ReplacerEnum.ConcurrentLRUReplacer, 81920)
                    .build();

            System.out.println("init finish.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Benchmark
    public void lru1024Test() throws Exception {
        for (int i = 0; i < lruBufferPoolInstance1024.getBufferPoolSize(); i++) {
            String dataPage = data.get(i);
            lruBufferPoolInstance1024.pinPage(Integer.parseInt(dataPage), true, dataPage);
        }
        System.out.println(lruBufferPoolInstance1024.getHitRate());
    }

    public static void main(String[] args) {
        try {
            Options options = new OptionsBuilder()
                    .include(JMHBPTest.class.getSimpleName())
                    .resultFormat(ResultFormatType.JSON)
                    .build();
            new Runner(options).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
