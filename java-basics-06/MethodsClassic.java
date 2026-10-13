// 実行: java MethodsClassic.java
public class MethodsClassic {
    // 完全な形では、main から直接呼ぶメソッドに static が必要
    static int add(int a, int b) {
        return a + b;
    }

    static int priceWithTax(int price) {
        return price + price * 10 / 100;
    }

    public static void main(String[] args) {
        System.out.println("3 + 4 = " + add(3, 4));
        System.out.println("1000円の税込み価格: " + priceWithTax(1000));
    }
}
