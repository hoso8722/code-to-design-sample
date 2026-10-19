// 実行: java Loops.java
public class Loops {
    public static void main(String[] args) {
        // for: 回数が決まっているとき
        int sumFor = 0;
        for (int i = 1; i <= 10; i++) {
            sumFor += i;
        }
        System.out.println("for で 1〜10 の合計: " + sumFor);

        // while: 条件が成り立つ間、繰り返す
        int sumWhile = 0;
        int n = 1;
        while (n <= 10) {
            sumWhile += n;
            n++;
        }
        System.out.println("while で 1〜10 の合計: " + sumWhile);

        // do-while: 先に1回実行してから、条件を調べる
        int count = 0;
        do {
            count++;
        } while (count < 0);   // 最初から条件は false だが、1回は実行される
        System.out.println("do-while の実行回数: " + count);

        int count2 = 0;
        while (count2 < 0) {
            count2++;
        }
        System.out.println("while の実行回数(条件が最初から false): " + count2);

        // 拡張 for: 配列やリストの要素を、順に取り出す
        int[] scores = {80, 65, 90};
        int total = 0;
        for (int score : scores) {
            total += score;
        }
        System.out.println("拡張 for で点数の合計: " + total);
    }
}
