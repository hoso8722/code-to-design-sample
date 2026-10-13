// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
void main() {
    int score = 85;
    String grade = switch (score / 10) {
        case 10, 9 -> "A";
        case 8 -> "B";
    };
    IO.println(grade);
}
