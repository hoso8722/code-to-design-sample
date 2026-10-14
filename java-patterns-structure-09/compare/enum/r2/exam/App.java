// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        for (String line : Samples.LINES) {
            CsvLine.Entry e = CsvLine.parse(line);
            Exam.judge(e.type(), e.student());
        }
    }
}
