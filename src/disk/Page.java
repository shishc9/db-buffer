package disk;

public class Page {

    public static final int PAGE_SIZE = 1024;

    public byte[] data;

    public Page() {
        data = new byte[PAGE_SIZE];
    }
}
