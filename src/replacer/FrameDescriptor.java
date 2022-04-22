package replacer;

import java.util.concurrent.atomic.AtomicInteger;

public class FrameDescriptor {

    public static final Integer INVALID_PAGE = -1;

    public FrameDescriptor() {
        pinCount = new AtomicInteger(0);
        fileName = null;
        isDirty = false;
        pinned = true;
        pageNum = INVALID_PAGE;
    }

    private String fileName;
    private Integer pageNum;
    private AtomicInteger pinCount;
    private boolean isDirty;
    private boolean pinned;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public AtomicInteger getPinCount() {
        return this.pinCount;
    }

    public void increasePinCount() {
        this.pinCount.incrementAndGet();
    }

    public void decreasePinCount() {
        this.pinCount.decrementAndGet();
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
