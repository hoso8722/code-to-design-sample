// 実行: java TwoDimensional.java
import java.util.Arrays;

void main() {
    // 3人の学生の、国語・数学・英語の点数
    int[][] scores = {
        {80, 65, 90},
        {70, 85, 60},
        {95, 90, 100}
    };

    IO.println("行の数: " + scores.length + "、1行目の列の数: " + scores[0].length);
    IO.println("2人目の数学: " + scores[1][1]);

    for (int i = 0; i < scores.length; i++) {
        int total = 0;
        for (int score : scores[i]) {
            total += score;
        }
        IO.println((i + 1) + "人目の合計: " + total + "、行: " + Arrays.toString(scores[i]));
    }

    // 行ごとに長さが違う配列も作れる
    int[][] jagged = new int[3][];
    jagged[0] = new int[] {1};
    jagged[1] = new int[] {1, 2};
    jagged[2] = new int[] {1, 2, 3};
    IO.println("行ごとに長さが違う配列: " + Arrays.deepToString(jagged));
}
