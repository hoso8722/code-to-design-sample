// 実行: java InitOrder.java
public class InitOrder {
    static class Config {
        static {
            System.out.println("  Config を初期化した（static 初期化子）");
        }
        static final String NAME = "config";        // コンパイル時の定数
        static String load() { return "loaded"; }
    }

    static class Heavy {
        static {
            System.out.println("  Heavy を初期化した（static 初期化子）");
        }
        static void use() {}
    }

    public static void main(String[] args) {
        System.out.println("1. main が始まった");
        System.out.println("2. Config.NAME を読む: " + Config.NAME);      // 定数は、初期化なしで読める
        System.out.println("3. Config.load() を呼ぶ");
        System.out.println("   → " + Config.load());                     // ここで初期化される
        System.out.println("4. Heavy の配列を作る: " + new Heavy[3].length); // 配列を作っても初期化されない
        System.out.println("5. Heavy.use() を呼ぶ");
        Heavy.use();                                                      // ここで初期化される
    }
}
