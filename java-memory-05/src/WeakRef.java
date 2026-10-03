// 実行: java WeakRef.java
import java.lang.ref.WeakReference;

public class WeakRef {
    public static void main(String[] args) {
        Object obj = new Object();
        WeakReference<Object> weak = new WeakReference<>(obj);

        System.gc();
        System.out.println("強い参照があるとき: " + weak.get());

        obj = null;  // 強い参照をなくす
        System.gc();
        System.out.println("強い参照がないとき: " + weak.get());
    }
}
