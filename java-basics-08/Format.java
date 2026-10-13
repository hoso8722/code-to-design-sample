// 実行: java Format.java
void main() {
    IO.println(String.format("%d 点", 75));
    IO.println(String.format("%5d|%-5d|%05d", 42, 42, 42));
    IO.println(String.format("%.2f", 3.14159));
    IO.println(String.format("%8.1f|", 72.456));
    IO.println(String.format("%,d", 1234567));
    IO.println(String.format("%s さんは %d 点で、割合は %.1f%%", "田中", 75, 75.0));

    // formatted: 文字列自身から呼べる(JDK 15 以降)
    IO.println("%s の平均は %.1f".formatted("数学", 80.0));
}
