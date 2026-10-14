// 検証しない: 複数ファイルの例。collect.sh で java -Xmx512m leak/Leak.java <モード> として実行している
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


// 短い間だけ使うリスナーを、長く生きる送り手に登録し続けたときの、GC後のヒープ使用量
public class Leak {
    interface Listener { void onResult(String name); }

    // 画面ごとに作られて、すぐ閉じられる受け手（2KBのデータを持つ）
    static class Screen implements Listener {
        final byte[] buffer = new byte[2048];
        @Override public void onResult(String name) { buffer[0]++; }
    }

    static class StrongPublisher {
        final List<Listener> listeners = new ArrayList<>();
        void add(Listener l) { listeners.add(l); }
        void remove(Listener l) { listeners.remove(l); }
    }

    static class WeakPublisher {
        final List<WeakReference<Listener>> listeners = new ArrayList<>();
        void add(Listener l) { listeners.add(new WeakReference<>(l)); }
    }

    static long usedMB() {
        for (int i = 0; i < 3; i++) System.gc();
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }

    public static void main(String[] args) {
        String mode = args[0];
        int screens = 100_000;
        long before = usedMB();
        StrongPublisher strong = new StrongPublisher();
        WeakPublisher weak = new WeakPublisher();
        for (int i = 0; i < screens; i++) {
            Screen screen = new Screen();
            switch (mode) {
                case "strong" -> strong.add(screen);                       // 登録して、解除しない
                case "unregister" -> { strong.add(screen); strong.remove(screen); }   // 閉じるときに解除する
                case "weak" -> weak.add(screen);                           // 弱い参照で登録する
            }
            screen.onResult("x");   // 画面を使って、閉じる（以後、画面への参照は送り手にしか残らない）
        }
        long after = usedMB();
        System.out.printf("%-10s 画面 %,d 個を作ったあとのGC後の使用量の増加: %d MB (送り手の登録数: %,d)%n", mode, screens, after - before,
            mode.equals("weak") ? weak.listeners.size() : strong.listeners.size());
        java.lang.ref.Reference.reachabilityFence(strong);
        java.lang.ref.Reference.reachabilityFence(weak);
    }
}
