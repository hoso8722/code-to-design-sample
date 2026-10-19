// 実行: java Recursion.java
public class Recursion {
    static long factorial(int n) {
        if (n <= 1) {
            return 1;                        // 終わりの条件
        }
        return n * factorial(n - 1);         // 自分自身を呼ぶ
    }

    static int depth = 0;

    static void deeper() {
        depth++;
        deeper();                            // 終わりの条件がない
    }

    public static void main(String[] args) {
        System.out.println("5! = " + factorial(5));
        System.out.println("20! = " + factorial(20));

        try {
            deeper();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError が起きた。呼び出しの深さは1000以上: " + (depth > 1000));
        }
    }
}
