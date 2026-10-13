// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
int grade(int score) {
    if (score >= 60) {
        return 1;
    }
}

void main() {
    IO.println(grade(80));
}
