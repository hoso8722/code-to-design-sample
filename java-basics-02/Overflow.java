// 実行: java Overflow.java
void main() {
    int max = Integer.MAX_VALUE;
    IO.println("Integer.MAX_VALUE + 1 = " + (max + 1));
    IO.println("Integer.MIN_VALUE - 1 = " + (Integer.MIN_VALUE - 1));

    long big = (long) max + 1;
    IO.println("long にして足すと = " + big);

    IO.println("2000000000 * 2 = " + 2000000000 * 2);
    IO.println("2000000000L * 2 = " + 2000000000L * 2);

    try {
        Math.addExact(max, 1);
    } catch (ArithmeticException e) {
        IO.println("Math.addExact は例外にする: " + e.getMessage());
    }
}
