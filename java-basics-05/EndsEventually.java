// 実行: java EndsEventually.java
void main() {
    // i-- で 5 未満であり続ける for。int の範囲を一周すると、条件が false になって終わる
    long start = System.nanoTime();
    long loops = 0;
    for (int i = 0; i < 5; i--) {
        loops++;
    }
    long millis = (System.nanoTime() - start) / 1_000_000;
    IO.println("繰り返した回数: " + loops);
    IO.println("終わるまでの時間は数秒以上: " + (millis > 1000));
}
