// 実行: java Order.java
// main を持つクラスより前に、ほかのクラスを書いたファイル
record Student(String name, int total) {}

public class Order {
    public static void main(String[] args) {
        System.out.println(new Student("Aさん", 215));
    }
}
