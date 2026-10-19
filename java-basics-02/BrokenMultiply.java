// 実行: java BrokenMultiply.java
public class BrokenMultiply {
    public static void main(String[] args) {
        int unitPrice = 1_500_000;
        int quantity = 2_000;
        int total = unitPrice * quantity;
        System.out.println("合計: " + total);
    }
}
