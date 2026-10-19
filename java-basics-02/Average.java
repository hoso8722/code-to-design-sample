// 実行: java Average.java
public class Average {
    public static void main(String[] args) {
        int total = 80 + 65 + 90;
        double average = (double) total / 3;
        System.out.println("平均点: " + average);
        System.out.println("小数第1位まで: " + String.format("%.1f", average));
    }
}
