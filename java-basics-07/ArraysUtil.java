// 実行: java ArraysUtil.java
import java.util.Arrays;
public class ArraysUtil {
    public static void main(String[] args) {
        int[] scores = {80, 65, 90, 72, 55};

        System.out.println("もとの配列: " + Arrays.toString(scores));
        Arrays.sort(scores);
        System.out.println("昇順に並べた: " + Arrays.toString(scores));
        System.out.println("最小値 = " + scores[0] + "、最大値 = " + scores[scores.length - 1]);

        int index = Arrays.binarySearch(scores, 72);
        System.out.println("72 の位置(並べ替え済みの配列で二分探索): " + index);

        int[] filled = new int[4];
        Arrays.fill(filled, 7);
        System.out.println("fill: " + Arrays.toString(filled));
    }
}
