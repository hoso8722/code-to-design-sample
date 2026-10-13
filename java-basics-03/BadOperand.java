// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
void main() {
    int score = 80;
    boolean inRange = 0 <= score <= 100;
    IO.println(inRange);
}
