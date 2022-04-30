package disk;

import java.util.Arrays;

public class Page {

    public static final int PAGE_SIZE = 1024;

    public byte[] data;

    public Page() {
        data = new byte[PAGE_SIZE];
    }

    public Page(byte[] arr) {
        data = arr;
    }

    @Override
    public String toString() {
        return "Page{" +
                "data=" + new String(data) +
                '}';
    }
}
