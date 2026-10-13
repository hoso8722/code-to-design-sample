// 実行: java TaxRounding.java
void main() {
    int price = 1005;
    int taxCut = price * 10 / 100;                       // 小数点以下は切り捨て
    long taxRounded = Math.round(price * 10 / 100.0);    // 四捨五入
    IO.println("切り捨て: " + taxCut + "、合計 " + (price + taxCut));
    IO.println("四捨五入: " + taxRounded + "、合計 " + (price + taxRounded));
}
