// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        for (Subject s : Samples.ALL) {
            System.out.println(s.getClass().getSimpleName() + ": " + s.score());
            System.out.println(s.getClass().getSimpleName() + ": " + s.describe());
            System.out.println(s.getClass().getSimpleName() + ": " + s.valid());
        }
    }
}
