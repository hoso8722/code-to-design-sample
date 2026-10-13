// 実行: java StringEquality.java
void main() {
    String a = "java";
    String b = "java";
    String c = new String("java");

    IO.println("a == b : " + (a == b));
    IO.println("a == c : " + (a == c));
    IO.println("a.equals(c) : " + a.equals(c));
    IO.println("c.intern() == a : " + (c.intern() == a));

    int x = 1000;
    int y = 1000;
    IO.println("基本型 x == y : " + (x == y));
}
