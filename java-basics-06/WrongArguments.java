// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
int add(int a, int b) {
    return a + b;
}

void main() {
    IO.println(add(1));
    IO.println(add("1", 2));
}
