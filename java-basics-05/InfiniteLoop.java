// 実行: java InfiniteLoop.java
public class InfiniteLoop {
    public static void main(String[] args) {
        // 更新の向きを間違えた for。条件はいつまでも true になり、終わらない。
        // このコードは確認用に、100ミリ秒たったら break で抜ける
        long deadline = System.nanoTime() + 100_000_000L;
        long loops = 0;
        for (int i = 0; i < 5; i--) {
            loops++;
            if (System.nanoTime() > deadline) {
                System.out.println("100ミリ秒たっても終わらない。i は負の値で減り続けている: " + (i < 0) + "、繰り返した回数は10万回以上: " + (loops > 100_000));
                break;
            }
        }
    }
}
