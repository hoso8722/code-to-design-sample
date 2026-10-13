// 実行: java ArraysUtil.java
import java.util.Arrays;

void main() {
    int[] scores = {80, 65, 90, 72, 55};

    IO.println("もとの配列: " + Arrays.toString(scores));
    Arrays.sort(scores);
    IO.println("昇順に並べた: " + Arrays.toString(scores));
    IO.println("最小値 = " + scores[0] + "、最大値 = " + scores[scores.length - 1]);

    int index = Arrays.binarySearch(scores, 72);
    IO.println("72 の位置(並べ替え済みの配列で二分探索): " + index);

    int[] filled = new int[4];
    Arrays.fill(filled, 7);
    IO.println("fill: " + Arrays.toString(filled));
}
