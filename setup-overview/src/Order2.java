// 実行: java Order2.java
// main を持つクラスを、ファイルの先頭に書いたファイル
public class Order2 {
    public static void main(String[] args) {
        System.out.println(new Student("Aさん", 215));
    }
}

record Student(String name, int total) {}
