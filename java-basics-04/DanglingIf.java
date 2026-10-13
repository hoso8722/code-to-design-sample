// 実行: java DanglingIf.java
void main() {
    int score = 40;
    if (score >= 60)
        IO.println("合格です");
        IO.println("おめでとう");   // インデントは if の中に見えるが、if の外
}
