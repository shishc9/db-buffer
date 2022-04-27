package interfac3;

/**
 * 发生页面替换的时候，返回被替换的页号和页面.
 */
public class PutVO<X, Y> {
    X key;
    Y value;
    String msg;

    public PutVO(X k, Y v, String msg) {
        this.key = k;
        this.value = v;
        this.msg = msg;
    }

    public X getKey() {
        return key;
    }

    public Y getValue() {
        return value;
    }

    public String getMsg() {
        return msg;
    }
}
