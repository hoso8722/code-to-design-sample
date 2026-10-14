// 実行: java Idle.java
// ヒープをあらかじめ確保した状態で、10秒間待つだけのプログラム（RSSを読むために使う）
public class Idle {
    public static void main(String[] args) throws Exception {
        Thread.sleep(10_000);
    }
}
