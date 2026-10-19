// 実行: java EvenSum.java
public class EvenSum {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 2; i <= 100; i += 2) {   // 2 から 100 まで、2 ずつ増やす
            sum += i;
        }
        System.out.println("1〜100 の偶数の合計: " + sum);
    }
}
