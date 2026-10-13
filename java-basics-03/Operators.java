// 実行: java Operators.java
void main() {
    int a = 17;
    int b = 5;
    IO.println("a + b = " + (a + b));
    IO.println("a - b = " + (a - b));
    IO.println("a * b = " + (a * b));
    IO.println("a / b = " + (a / b));
    IO.println("a % b = " + (a % b));
    IO.println("-7 % 3 = " + (-7 % 3) + "、Math.floorMod(-7, 3) = " + Math.floorMod(-7, 3));

    IO.println("a > b : " + (a > b));
    IO.println("a == b : " + (a == b));
    IO.println("a != b : " + (a != b));

    boolean passed = true;
    boolean attended = false;
    IO.println("passed && attended : " + (passed && attended));
    IO.println("passed || attended : " + (passed || attended));
    IO.println("!passed : " + !passed);

    int total = 10;
    total += 5;    // total = total + 5
    total *= 2;    // total = total * 2
    IO.println("複合代入の結果: " + total);

    int x = 5;
    x += 2.5;      // x = (int) (x + 2.5) と同じ。小数点以下は切り捨て
    IO.println("int に 2.5 を += した結果: " + x);
}
