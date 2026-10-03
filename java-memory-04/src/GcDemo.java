// 実行: java -Xmx512m -Xlog:gc GcDemo.java short
public class GcDemo {
    static Object sink;

    public static void main(String[] args) {
        // short: 1KBのオブジェクトを作り、すぐ不要にする
        // long : 最近作った10万個（約100MB）を持ち続け、古いものから手放す
        // huge : 1MBの配列を作り、すぐ不要にする
        String mode = args.length > 0 ? args[0] : "short";
        int size = mode.equals("huge") ? 1024 * 1024 : 1024;
        byte[][] recent = new byte[100_000][];

        long end = System.nanoTime() + 5_000_000_000L;  // 5秒間
        long count = 0;
        while (System.nanoTime() < end) {
            byte[] data = new byte[size];
            if (mode.equals("long")) {
                recent[(int) (count % recent.length)] = data;  // 10万個前のものと入れ替える
            } else {
                sink = data;  // すぐ次のオブジェクトに置き換わる
            }
            count++;
        }
        System.out.println(mode + ": allocated " + count + " objects");
    }
}
