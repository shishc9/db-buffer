package disk;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class DBFile {

    public static class PageNotAllocatedException extends RuntimeException {}
    public static class EmptyFileException extends RuntimeException {}
    public static class IllegalPageNumberException extends RuntimeException {}
    public static class NonPositiveRunSizeException extends RuntimeException {}
    public static class FileFullException extends RuntimeException {}

    private String dataFileName;
    private String mapFileName;
    private Integer numPages;

    /**
     * 创建指定页数的数据库，且这个数据库的页数永远不会改变.
     * @param name 数据库名称.
     * @param numPages 指定页数.
     */
    public DBFile(String name, Integer numPages) throws IOException {
        // 指定页数最小为2.
        if (numPages < 2) {
            numPages = 2;
        }

        // dataFileName  eg.[test.db]
        dataFileName = name + ".db";
        RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "rw");
        dataFile.setLength(numPages * Page.PAGE_SIZE);
        byte[] data = new byte[numPages * Page.PAGE_SIZE];
        dataFile.write(data);
        dataFile.close();

        // mapFileName  eg.[test.db.map]
        mapFileName = dataFileName + ".map";
        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "rw");
        mapFile.setLength(numPages);
        byte[] data2 = new byte[numPages];
        mapFile.write(data2);
        mapFile.close();

        this.numPages = numPages;
    }

//    public DBFile(String name) {
//
//    }

    /**
     * 删除一个数据库.
     * 注意：如果有对象引用该数据库，这样做很危险.
     * @param name 数据库名称
     * @return true/false. false: .db/.map中有一个删除失败.
     */
    public static boolean erase(String name) {
        boolean isDelete = new File(name).delete();
        if (isDelete) {
            isDelete = new File(name + ".map").delete();
        }
        return isDelete;
    }

    public Integer allocatePages(Integer runSize) throws IOException{
        if (runSize <= 0) {
            throw new NonPositiveRunSizeException();
        }

        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "rw");
        mapFile.seek(0);
        byte[] map = new byte[numPages];
        mapFile.readFully(map);

        for (int i = 0; i < mapFile.length() - runSize + 1; i++) {
            int currentRunSize = 0;
            for (int j = i; j < i + runSize; j++) {
                if (map[j] != 0) {
                    break;
                }
                currentRunSize ++;
            }

            if (currentRunSize == runSize) {
                byte[] mapUpdate = new byte[runSize];
                Arrays.fill(mapUpdate, (byte) 1);
                mapFile.seek(i);
                mapFile.write(mapUpdate);
                mapFile.close();
                return i;
            }
        }

        mapFile.close();
        throw new FileFullException();
    }

    public void writePage(Integer pageNum, Page page) throws IOException{
        if (numPages == 0) {
            throw new EmptyFileException();
        }
        if (pageNum > numPages - 1 || pageNum < 0) {
            throw new IllegalPageNumberException();
        }

        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "rw");
        mapFile.seek(pageNum);
        byte[] pagePos = new byte[1];
        mapFile.readFully(pagePos);
        if (pagePos[0] == 0) {
            throw new PageNotAllocatedException();
        }
        mapFile.close();

        RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "rw");
        dataFile.seek(pageNum * Page.PAGE_SIZE);
        dataFile.write(page.data);
        dataFile.close();
    }
}
