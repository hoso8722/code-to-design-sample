// 実行: java Operators.java
public class Operators {
    public static void main(String[] args) {
        int a = 17;
        int b = 5;
        System.out.println("a + b = " + (a + b));
        System.out.println("a - b = " + (a - b));
        System.out.println("a * b = " + (a * b));
        System.out.println("a / b = " + (a / b));
        System.out.println("a % b = " + (a % b));
        System.out.println("-7 % 3 = " + (-7 % 3) + "、Math.floorMod(-7, 3) = " + Math.floorMod(-7, 3));

        System.out.println("a > b : " + (a > b));
        System.out.println("a == b : " + (a == b));
        System.out.println("a != b : " + (a != b));

        boolean passed = true;
        boolean attended = false;
        System.out.println("passed && attended : " + (passed && attended));
        System.out.println("passed || attended : " + (passed || attended));
        System.out.println("!passed : " + !passed);

        int total = 10;
        total += 5;    // total = total + 5
        total *= 2;    // total = total * 2
        System.out.println("複合代入の結果: " + total);

        int x = 5;
        x += 2.5;      // x = (int) (x + 2.5) と同じ。小数点以下は切り捨て
        System.out.println("int に 2.5 を += した結果: " + x);
    }
}
