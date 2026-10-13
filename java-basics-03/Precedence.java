// 実行: java Precedence.java
void main() {
    IO.println("1 + 2 * 3 = " + (1 + 2 * 3));
    IO.println("(1 + 2) * 3 = " + ((1 + 2) * 3));

    // && は || より先に計算される
    IO.println("true || false && false = " + (true || false && false));
    IO.println("(true || false) && false = " + ((true || false) && false));

    // 文字列との + は、左から順に計算される
    IO.println("合計: " + 1 + 2);
    IO.println("合計: " + (1 + 2));
    IO.println(1 + 2 + "個");
}
