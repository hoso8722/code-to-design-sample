// 実行: java DanglingIf.java
public class DanglingIf {
    public static void main(String[] args) {
        int score = 40;
        if (score >= 60)
            System.out.println("合格です");
            System.out.println("おめでとう");   // インデントは if の中に見えるが、if の外
    }
}
