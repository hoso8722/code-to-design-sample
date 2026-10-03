// 実行: java Histogram.java
// 別のターミナルで: jcmd <pid> GC.class_histogram
import java.util.HashMap;
import java.util.Map;

public class Histogram {
    public static void main(String[] args) throws Exception {
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 1_000_000; i++) map.put(i, i);
        System.out.println("pid=" + ProcessHandle.current().pid() + " size=" + map.size());
        Thread.sleep(30_000);  // この間に jcmd で調べる
    }
}
