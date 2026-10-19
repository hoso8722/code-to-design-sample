// 実行: java StringEquality.java
public class StringEquality {
    public static void main(String[] args) {
        String a = "java";
        String b = "java";
        String c = new String("java");

        System.out.println("a == b : " + (a == b));
        System.out.println("a == c : " + (a == c));
        System.out.println("a.equals(c) : " + a.equals(c));
        System.out.println("c.intern() == a : " + (c.intern() == a));

        int x = 1000;
        int y = 1000;
        System.out.println("基本型 x == y : " + (x == y));
    }
}
