// 実行: java Aliasing.java
import java.util.Arrays;

public class Aliasing {
    static void reset(int[] scores) {
        Arrays.fill(scores, 0);
    }

    public static void main(String[] args) {
        int[] original = {80, 65, 90};
        int[] copy = Arrays.copyOf(original, original.length);

        reset(original);
        System.out.println("reset のあとの original: " + Arrays.toString(original));
        System.out.println("事前にコピーしておいた copy: " + Arrays.toString(copy));
    }
}
