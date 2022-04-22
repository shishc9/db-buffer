package test;

import bufferpool.BufferPoolInstance;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BufferPoolInstanceTest {

    @Test
    public void testBuilder() throws IOException {
        BufferPoolInstance bufferPoolInstance = new BufferPoolInstance.BufferPoolBuilder()
                .setReplacer(BufferPoolInstance.ReplacerEnum.SingleListLRUReplacer, 16)
                .setDBFile("shishc", 32)
                .build();

        System.out.println("replacer:" + bufferPoolInstance.getReplacer().getClass());
        System.out.println("dbfile: " + bufferPoolInstance.getDbFile().getDataFileName());
    }

}
