// 実行: java ArrayBasics.java
public class ArrayBasics {
    public static void main(String[] args) {
        // 宣言と初期化の書き方
        int[] scores = {80, 65, 90};           // 値を並べて初期化
        int[] empty = new int[5];              // 長さ5。要素は 0 で初期化される
        String[] names = new String[3];        // 参照型の要素は null で初期化される
        boolean[] flags = new boolean[2];      // boolean は false

        System.out.println("scores.length = " + scores.length);
        System.out.println("scores[0] = " + scores[0] + "、scores[2] = " + scores[2]);
        System.out.println("empty = " + java.util.Arrays.toString(empty));
        System.out.println("names = " + java.util.Arrays.toString(names));
        System.out.println("flags = " + java.util.Arrays.toString(flags));

        scores[1] = 70;                        // 要素を書き換える
        System.out.println("書き換え後 = " + java.util.Arrays.toString(scores));

        System.out.println("配列をそのまま println すると = " + scores.toString().startsWith("[I@"));
    }
}
