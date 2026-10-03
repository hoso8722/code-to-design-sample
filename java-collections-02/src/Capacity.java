// 実行: java --add-opens java.base/java.util=ALL-UNNAMED Capacity.java
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Capacity {
    static final Field ELEMENT_DATA;
    static {
        try {
            ELEMENT_DATA = ArrayList.class.getDeclaredField("elementData");  // 内部の配列（非公開のフィールド）
            ELEMENT_DATA.setAccessible(true);  // 実行時に --add-opens が必要
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static int capacity(ArrayList<?> list) throws IllegalAccessException {
        return ((Object[]) ELEMENT_DATA.get(list)).length;
    }

    static long allocatedBytes() {
        var bean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        return bean.getCurrentThreadAllocatedBytes();
    }

    public static void main(String[] args) throws Exception {
        // 1. 追加していくと、内部の配列の大きさがいつ変わるか
        ArrayList<Integer> list = new ArrayList<>();
        System.out.println("作った直後の容量: " + capacity(list));
        System.out.println("size,capacity");
        int last = capacity(list);
        long copied = 0;  // 作り直しのときにコピーした要素の数
        for (int i = 0; i < 1_000_000; i++) {
            list.add(i);
            int now = capacity(list);
            if (now != last) {
                copied += list.size() - 1;
                System.out.println(list.size() + "," + now);
                last = now;
            }
        }
        System.out.printf("100万個を追加し終えた容量: %,d（コピーした要素の合計: %,d）%n", capacity(list), copied);
        list.trimToSize();
        System.out.printf("trimToSize() のあとの容量: %,d%n", capacity(list));

        // 2. 容量を指定しない場合と、指定した場合の確保バイト数（Integer は -128〜127 以外なので毎回作られる）
        for (int round = 0; round < 3; round++) {  // 1、2回目は準備運動
            long before = allocatedBytes();
            ArrayList<Integer> a = new ArrayList<>();
            for (int i = 0; i < 1_000_000; i++) a.add(1_000);
            long middle = allocatedBytes();
            ArrayList<Integer> b = new ArrayList<>(1_000_000);
            for (int i = 0; i < 1_000_000; i++) b.add(1_000);
            long after = allocatedBytes();
            if (round == 2) {
                System.out.printf("容量を指定しない: %.1f MB、指定する: %.1f MB（要素 %d, %d 個）%n",
                        (middle - before) / 1e6, (after - middle) / 1e6, a.size(), b.size());
            }
        }
    }
}
