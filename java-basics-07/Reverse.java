// 実行: java Reverse.java
import java.util.Arrays;
public class Reverse {
    public static void main(String[] args) {
        int[] scores = {80, 65, 90, 72, 55};
        int[] reversed = new int[scores.length];
        for (int i = 0; i < scores.length; i++) {
            reversed[i] = scores[scores.length - 1 - i];
        }
        System.out.println("元の配列: " + Arrays.toString(scores));
        System.out.println("逆順: " + Arrays.toString(reversed));
    }
}
