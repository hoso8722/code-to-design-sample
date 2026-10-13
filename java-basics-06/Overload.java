// 実行: java Overload.java
// 同じ名前で、引数の型が違うメソッド(オーバーロード)
String show(int value) {
    return "int: " + value;
}

String show(double value) {
    return "double: " + value;
}

String show(String value) {
    return "String: " + value;
}

String show(int a, int b) {
    return "int, int: " + a + ", " + b;
}

void main() {
    IO.println(show(5));
    IO.println(show(5.5));
    IO.println(show("五"));
    IO.println(show(1, 2));
    IO.println(show('A'));      // char は int に変換される
    IO.println(show(5L));       // long は double に変換される(int には入らない)
}
