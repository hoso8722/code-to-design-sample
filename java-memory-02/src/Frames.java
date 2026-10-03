// 実行: java Frames.java
public class Frames {
    record Point(int x, int y) {}

    static int total(int[] scores) {
        int sum = 0;
        for (int s : scores) {
            sum += s;  // ← 最後の繰り返し（s = 90）でここを実行した直後が図の状態
        }
        return sum;
    }

    public static void main(String[] args) {
        int count = 2;
        Point p = new Point(1, 2);
        int[] scores = {80, 90};
        System.out.println(total(scores) + " " + p + " " + count);
    }
}
