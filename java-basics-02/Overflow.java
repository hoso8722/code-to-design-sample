// 実行: java Overflow.java
public class Overflow {
    public static void main(String[] args) {
        int max = Integer.MAX_VALUE;
        System.out.println("Integer.MAX_VALUE + 1 = " + (max + 1));
        System.out.println("Integer.MIN_VALUE - 1 = " + (Integer.MIN_VALUE - 1));

        long big = (long) max + 1;
        System.out.println("long にして足すと = " + big);

        System.out.println("2000000000 * 2 = " + 2000000000 * 2);
        System.out.println("2000000000L * 2 = " + 2000000000L * 2);

        try {
            Math.addExact(max, 1);
        } catch (ArithmeticException e) {
            System.out.println("Math.addExact は例外にする: " + e.getMessage());
        }
    }
}
