// 実行: java Overload.java
// 同じ名前で、引数の型が違うメソッド(オーバーロード)
public class Overload {
    static String show(int value) {
        return "int: " + value;
    }

    static String show(double value) {
        return "double: " + value;
    }

    static String show(String value) {
        return "String: " + value;
    }

    static String show(int a, int b) {
        return "int, int: " + a + ", " + b;
    }

    public static void main(String[] args) {
        System.out.println(show(5));
        System.out.println(show(5.5));
        System.out.println(show("五"));
        System.out.println(show(1, 2));
        System.out.println(show('A'));      // char は int に変換される
        System.out.println(show(5L));       // long は double に変換される(int には入らない)
    }
}
