// 実行: java Ternary.java
void main() {
    int score = 72;
    String result = score >= 60 ? "合格" : "不合格";
    IO.println(result);

    // 三項演算子の入れ子は読みにくい。if の連続や switch 式を使うほうがよい
    String grade = score >= 90 ? "A" : score >= 80 ? "B" : score >= 60 ? "C" : "D";
    IO.println(grade);
}
