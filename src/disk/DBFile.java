package disk;

import exception.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class DBFile {

    private String dataFileName;

    public String getDataFileName() {
        return dataFileName;
    }

    public String getMapFileName() {
        return mapFileName;
    }

    public Integer getNumPages() {
        return numPages;
    }

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

    /**
     * 分配指定大小的数据页，并返回第一个可用的页号.
     * 文件空间的分配策略不会使文件产生碎片.
     * @param runSize 指定大小.
     * @return 第一个可用的页号.
     */
    public Integer allocatePages(Integer runSize) throws IOException {
        // 参数检查
        if (runSize <= 0) {
            throw new NonPositiveRunSizeException();
        }

        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "rw");
        mapFile.seek(0);
        byte[] map = new byte[numPages];
        mapFile.readFully(map);

        for (int i = 0; i < mapFile.length() - runSize + 1; i++) {
            int currentRunSize = 0;
            // 从当前位置开始，偏移量为runSize，找一段连续且为未被分配的空间.
            for (int j = i; j < i + runSize; j++) {
                if (map[j] != 0) {
                    break;
                }
                currentRunSize ++;
            }
            // 已经找到了.
            if (currentRunSize == runSize) {
                byte[] mapUpdate = new byte[runSize];
                Arrays.fill(mapUpdate, (byte) 1);
                mapFile.seek(i);
                mapFile.write(mapUpdate);
                mapFile.close();
                return i;
            }
        }

        // 若未找到连续且runSize大小的空间，说明文件已满.
        mapFile.close();
        throw new FileFullException();
    }

    /**
     * 从起始页开始，偏移量为runSize的页面全部归为未分配状态.
     * @param startPageNum 起始页.
     * @param runSize 偏移量.
     */
    public void deallocatePages(Integer startPageNum, Integer runSize) throws IOException {
        // 参数校验
        if (runSize < 0) {
            throw new NonPositiveRunSizeException();
        }
        if (startPageNum < 0 || startPageNum >= numPages || startPageNum + runSize > numPages) {
            throw new IllegalPageNumberException();
        }

        byte[] mapUpdate = new byte[runSize];
        // runSize大小的全0数组.
        Arrays.fill(mapUpdate, (byte) 0);
        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "rw");
        mapFile.seek(startPageNum);
        mapFile.write(mapUpdate);
        mapFile.close();

        //TODO: .db文件的page也该删除.
    }

    /**
     * 写入页号为pageNum的页面page.
     * @param pageNum 页号.
     * @param page 页面数据.
     */
    public void writePage(Integer pageNum, Page page) throws IOException {
        // 参数检验
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
        // 当前页号还未被分配.
        if (pagePos[0] == 0) {
            throw new PageNotAllocatedException();
        }
        mapFile.close();

        // 写入页面.
        RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "rw");
        dataFile.seek(pageNum * Page.PAGE_SIZE);
        dataFile.write(page.data);
        dataFile.close();
    }

    /**
     * 读取页号为pageNum的页面并写入到page.data中.
     * @param pageNum 页号.
     * @param page 待写入页面.
     */
    public void readPage(Integer pageNum, Page page) throws IOException {
        if (pageNum < 0 || pageNum > numPages) {
            throw new IllegalPageNumberException();
        }

        RandomAccessFile mapFile = new RandomAccessFile(mapFileName, "r");
        mapFile.seek(pageNum);
        byte[] map = new byte[1];
        mapFile.readFully(map);
        // 确保该页面已经分配.
        if (map[0] == 0) {
            throw new PageNotAllocatedException();
        }
        mapFile.close();

        RandomAccessFile dataFile = new RandomAccessFile(dataFileName, "r");
        dataFile.seek(pageNum * Page.PAGE_SIZE);
        dataFile.readFully(page.data);
        dataFile.close();
    }
}
