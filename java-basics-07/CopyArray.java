// 実行: java CopyArray.java
import java.util.Arrays;
public class CopyArray {
    public static void main(String[] args) {
        int[] a = {1, 2, 3};

        int[] b = a;                           // 参照のコピー。同じ配列を指す
        b[0] = 99;
        System.out.println("b = a のあとで b[0] を変更: a = " + Arrays.toString(a));

        int[] c = Arrays.copyOf(a, a.length);  // 中身のコピー。別の配列になる
        c[1] = 77;
        System.out.println("Arrays.copyOf のあとで c[1] を変更: a = " + Arrays.toString(a) + "、c = " + Arrays.toString(c));

        int[] longer = Arrays.copyOf(a, 5);    // 長さを変えて、コピーできる。足りない分は 0
        System.out.println("長さ5にコピー: " + Arrays.toString(longer));

        int[] d = Arrays.copyOf(a, a.length);
        System.out.println("a == b : " + (a == b) + "、a == d : " + (a == d));
        System.out.println("Arrays.equals(a, c) : " + Arrays.equals(a, c) + "、Arrays.equals(a, d) : " + Arrays.equals(a, d));
    }
}
