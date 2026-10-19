// 実行: java EndsEventually.java
public class EndsEventually {
    public static void main(String[] args) {
        // i-- で 5 未満であり続ける for。int の範囲を一周すると、条件が false になって終わる
        long start = System.nanoTime();
        long loops = 0;
        for (int i = 0; i < 5; i--) {
            loops++;
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("繰り返した回数: " + loops);
        System.out.println("かかった時間: " + millis + " ミリ秒");
    }
}
