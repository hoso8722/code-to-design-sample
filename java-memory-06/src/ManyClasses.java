// 実行: java ManyClasses.java 20000
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

public class ManyClasses {
    // これと同じ中身のクラスを、ヒドゥンクラスとして何個も定義する
    public static class Template {
        public int value;
        public int twice() { return value * 2; }
    }

    static long poolUsedKB(String name) {
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().equals(name)) return pool.getUsage().getUsed() / 1024;
        }
        throw new IllegalArgumentException(name);
    }

    public static void main(String[] args) throws Exception {
        int total = args.length > 0 ? Integer.parseInt(args[0]) : 20_000;
        byte[] bytes;
        try (InputStream in = ManyClasses.class.getResourceAsStream("ManyClasses$Template.class")) {
            bytes = in.readAllBytes();  // Template のクラスファイルの中身
        }

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        List<Class<?>> classes = new ArrayList<>();  // クラスを参照し続けて、アンロードされないようにする
        System.out.println("phase,classes,metaspace_kb,class_space_kb,loaded_classes");
        for (int i = 0; i <= total; i++) {
            if (i > 0) classes.add(lookup.defineHiddenClass(bytes, true).lookupClass());
            if (i % (total / 10) == 0) print("define", classes.size());
        }

        classes.clear();  // クラスへの参照をなくす
        System.gc();      // 参照されていないヒドゥンクラスは、GCでアンロードされる
        print("after-gc", classes.size());
    }

    static void print(String phase, int count) {
        System.out.printf("%s,%d,%d,%d,%d%n", phase, count, poolUsedKB("Metaspace"),
                poolUsedKB("Compressed Class Space"), ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
    }
}
