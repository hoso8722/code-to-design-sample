// 実行: java Division.java
public class Division {
    public static void main(String[] args) {
        System.out.println("7 / 2 = " + 7 / 2);
        System.out.println("7 % 2 = " + 7 % 2);
        System.out.println("7 / 2.0 = " + 7 / 2.0);
        System.out.println("(double) 7 / 2 = " + (double) 7 / 2);
        System.out.println("(double) (7 / 2) = " + (double) (7 / 2));

        // 価格に税を足す: int と double で、結果が変わる
        int price = 1005;
        int taxInt = price * 10 / 100;
        double taxDouble = price * 10 / 100.0;
        System.out.println("int で求めた税額: " + taxInt);
        System.out.println("double で求めた税額: " + taxDouble);

        System.out.println("1.0 / 0 = " + 1.0 / 0);
    }
}
