// 実行: java RandomPrediction.java
import java.util.Random;

public class RandomPrediction {
    private static final long MULTIPLIER = 0x5DEECE66DL;
    private static final long ADDEND = 0xBL;
    private static final long MASK = (1L << 48) - 1;

    // java.util.Random の内部の計算を、そのまま再現する(48ビットの状態から、上位32ビットを返す)
    static int next32(long state) {
        return (int) (state >>> 16);
    }

    static long advance(long state) {
        return (state * MULTIPLIER + ADDEND) & MASK;
    }

    // 連続して出力された2つの nextInt() から、内部の状態を割り出す
    // 1つ目の出力は、状態の上位32ビット。下位16ビットは不明なので、65,536通りを試す
    static long recoverState(int first, int second) {
        for (long low = 0; low < (1 << 16); low++) {
            long candidate = (((long) first & 0xFFFFFFFFL) << 16) | low;
            if (next32(advance(candidate)) == second) {
                return advance(candidate); // 2つ目の出力を出した直後の状態
            }
        }
        throw new IllegalStateException("状態が見つからない");
    }

    public static void main(String[] args) {
        // 利用者に配るトークンを、java.util.Random で作っている、とする
        Random random = new Random();
        int token1 = random.nextInt();
        int token2 = random.nextInt();
        System.out.println("観測した2つのトークン: " + token1 + ", " + token2);

        // 攻撃する側は、この2つだけから、内部の状態を割り出す
        long state = recoverState(token1, token2);
        int predicted = next32(advance(state));

        int actual = random.nextInt();
        System.out.println("3つ目の予測:   " + predicted);
        System.out.println("3つ目の実際の値: " + actual);
        System.out.println("予測が当たったか: " + (predicted == actual));
    }
}
