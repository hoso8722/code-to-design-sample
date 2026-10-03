// 実行: java IntegerEquals.java
public class IntegerEquals {
    public static void main(String[] args) {
        Integer a = 127, b = 127;
        Integer c = 1000, d = 1000;
        System.out.println(a == b);       // true（キャッシュされた同じオブジェクト）
        System.out.println(c == d);       // false（別々のオブジェクト）
        System.out.println(c.equals(d));  // true
    }
}
