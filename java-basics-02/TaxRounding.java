// 実行: java TaxRounding.java
public class TaxRounding {
    public static void main(String[] args) {
        int price = 1005;
        int taxCut = price * 10 / 100;                       // 小数点以下は切り捨て
        long taxRounded = Math.round(price * 10 / 100.0);    // 四捨五入
        System.out.println("切り捨て: " + taxCut + "、合計 " + (price + taxCut));
        System.out.println("四捨五入: " + taxRounded + "、合計 " + (price + taxRounded));
    }
}
