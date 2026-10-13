// 実行: java Division.java
void main() {
    IO.println("7 / 2 = " + 7 / 2);
    IO.println("7 % 2 = " + 7 % 2);
    IO.println("7 / 2.0 = " + 7 / 2.0);
    IO.println("(double) 7 / 2 = " + (double) 7 / 2);
    IO.println("(double) (7 / 2) = " + (double) (7 / 2));

    // 価格に税を足す: int と double で、結果が変わる
    int price = 1005;
    int taxInt = price * 10 / 100;
    double taxDouble = price * 10 / 100.0;
    IO.println("int で求めた税額: " + taxInt);
    IO.println("double で求めた税額: " + taxDouble);

    IO.println("1.0 / 0 = " + 1.0 / 0);
}
