package bufferpool;

import disk.DBFile;
import disk.Page;
import interfac3.Replacer;
import replacer.ConcurrentLRUReplacer;
import replacer.LFUReplacer;
import replacer.LRUReplacer;
import replacer.SingleListLRUReplacer;

import java.io.IOException;

public class BufferPoolInstance {

    private static final Integer REPLACER_SIZE = 16;
    private static final Integer PAGE_NUM_SIZE = 32;
    private static final String DEFAULT_DB_FILE_NAME = "example";

    private FrameDescriptor[] frameTable;
    private Replacer<Integer, Page> replacer;
    private DBFile dbFile;

//    public BufferPoolInstance() throws IOException {
//        replacer = new LRUReplacer<>(REPLACER_SIZE);
//        dbFile = new DBFile(DEFAULT_DB_FILE_NAME, PAGE_NUM_SIZE);
//    }

    public enum ReplacerEnum {
        LRUReplacer,
        LFUReplacer,
        SingleListLRUReplacer,
        ConcurrentLRUReplacer
    }

    public static class BufferPoolBuilder {

        private BufferPoolInstance bufferPoolInstance;

        public BufferPoolBuilder() throws IOException {
            bufferPoolInstance = new BufferPoolInstance();
        }

        public BufferPoolBuilder setReplacer(ReplacerEnum replacerName, Integer initSize) {
            switch (replacerName) {
                case LFUReplacer: {
                    bufferPoolInstance.replacer = new LFUReplacer<>(initSize);
                    break;
                }
                case LRUReplacer: {
                    bufferPoolInstance.replacer = new LRUReplacer<>(initSize);
                    break;
                }
                case SingleListLRUReplacer: {
                    bufferPoolInstance.replacer = new SingleListLRUReplacer<>(initSize);
                    break;
                }
                case ConcurrentLRUReplacer: {
                    bufferPoolInstance.replacer = new ConcurrentLRUReplacer<>(initSize);
                    break;
                }
            }
            return this;
        }

        public BufferPoolBuilder setDBFile(String name, Integer pageNums) throws IOException {
            bufferPoolInstance.dbFile = new DBFile(name, pageNums);
            return this;
        }

        public BufferPoolInstance build() {
            return bufferPoolInstance;
        }
    }

    public Replacer<Integer, Page> getReplacer() {
        return this.replacer;
    }

    public DBFile getDbFile() {
        return this.dbFile;
    }

    public FrameDescriptor[] getFrameTable() {
        return frameTable;
    }
}
