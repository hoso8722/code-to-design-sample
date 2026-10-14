// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        for (Subject s : Samples.ALL) {
            System.out.println(s.getClass().getSimpleName() + ": " + Subjects.score(s));
            System.out.println(s.getClass().getSimpleName() + ": " + Subjects.describe(s));
            System.out.println(s.getClass().getSimpleName() + ": " + Subjects.valid(s));
            System.out.println(s.getClass().getSimpleName() + ": " + Subjects.maxPoints(s));
        }
    }
}
