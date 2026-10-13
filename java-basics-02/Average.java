// 実行: java Average.java
void main() {
    int total = 80 + 65 + 90;
    double average = (double) total / 3;
    IO.println("平均点: " + average);
    IO.println("小数第1位まで: " + String.format("%.1f", average));
}
