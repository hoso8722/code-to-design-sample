// 実行: java SumRecursion.java
public class SumRecursion {
    static int sumTo(int n) {
        if (n == 0) {
            return 0;
        }
        return n + sumTo(n - 1);
    }

    public static void main(String[] args) {
        System.out.println("1 から 10 の合計: " + sumTo(10));
        System.out.println("1 から 100 の合計: " + sumTo(100));
    }
}
