// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        StudentSource source = new CsvV2Adapter();
        Judge judge = new LoggingJudge(new ScoreJudge());
        for (String line : Samples.LINES) {
            StudentSource.Entry e = source.parse(line);
            judge.passes(e.type(), e.student());
        }
    }
}
