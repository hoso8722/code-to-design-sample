// 実行: java Precedence.java
public class Precedence {
    public static void main(String[] args) {
        System.out.println("1 + 2 * 3 = " + (1 + 2 * 3));
        System.out.println("(1 + 2) * 3 = " + ((1 + 2) * 3));

        // && は || より先に計算される
        System.out.println("true || false && false = " + (true || false && false));
        System.out.println("(true || false) && false = " + ((true || false) && false));

        // 文字列との + は、左から順に計算される
        System.out.println("合計: " + 1 + 2);
        System.out.println("合計: " + (1 + 2));
        System.out.println(1 + 2 + "個");
    }
}
