package icu.shishc.test.disk;

import icu.shishc.disk.DBFile;
import icu.shishc.disk.Page;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class DBFileTest {

    private DBFile dbFile;

    @Before
    public void dbFileInit() throws IOException {
        dbFile = new DBFile("db-icu.shishc.test", 5);
    }

    @Test
    public void allocatePagesTest() throws IOException {
        System.out.println("allocate size3: " + dbFile.allocatePages(3));
        System.out.println("allocate size2: " + dbFile.allocatePages(1));
        System.out.println("allocate size1: " + dbFile.allocatePages(1));
        System.out.println("allocate size1: " + dbFile.allocatePages(1));
    }

    @Test
    public void deallocatePagesTest() throws IOException {
        System.out.println("allocate size3: " + dbFile.allocatePages(3));
        System.out.println("allocate size2: " + dbFile.allocatePages(1));
        System.out.println("allocate size2: " + dbFile.allocatePages(1));
        dbFile.deallocatePages(2, 2);
    }

    @Test
    public void writePageTest() throws IOException{
        Integer pageId1 = dbFile.allocatePages(1);
        System.out.println("allocate size3: " + pageId1);
        String str = "writePagetest";
        byte[] bytes = str.getBytes();
        dbFile.writePage(pageId1, new Page(bytes));
    }

    @Test
    public void readPageTest() throws IOException {
        writePageTest();
        Page page1 = new Page();
        // throw Exception
        //dbFile.readPage(2, page1);
        Page page2 = new Page();
        dbFile.readPage(0, page2);
        System.out.println(Arrays.toString(page2.data));
    }

}
