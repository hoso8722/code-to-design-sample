// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class App {
    public static void main(String[] args) {
        StudentSource source = new CsvV1Adapter();
        List<String> lines = List.of("佐藤,60,70,80", "鈴木,90,50,40", "高橋,70,70,50");
        System.out.println("合格: " + new ExamService(source).countPassed(lines));
        System.out.println("一覧: " + new ReportPrinter(source).summarize(lines));
        System.out.println("最高点: " + new Ranking(source).topScorer(lines));
    }
}
