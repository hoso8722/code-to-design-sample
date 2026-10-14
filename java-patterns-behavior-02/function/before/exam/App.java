// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        System.out.println(new ExamProcess("一般", s -> s.total() >= 180).run(Samples.LINES));
        System.out.println(new ExamProcess("奨学", s -> s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60).run(Samples.LINES));
        System.out.println(new ExamProcess("編入", s -> s.math() + s.english() >= 120).run(Samples.LINES));
    }
}
