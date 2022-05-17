package test.replacer;

import disk.Page;
import org.openjdk.jmh.annotations.*;
import replacer.SingleListLRUReplacer;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ReplacerTest {

//    SingleListLRUReplacer<Integer, Page> singleListReplacer = new SingleListLRUReplacer<>();

    static {

    }


    @Benchmark
    public void singleListTest() {

    }

    @Benchmark
    public void lruTest() {

    }

    public static void main(String[] args) {

    }

}
