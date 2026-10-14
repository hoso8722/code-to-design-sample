// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class App {
    public static void main(String[] args) {
        StudentSource source = new CsvV2Adapter();
        List<String> lines = List.of("101,80,60,70,佐藤", "102,40,90,50,鈴木", "103,50,70,70,高橋");
        System.out.println("合格: " + new ExamService(source).countPassed(lines));
        System.out.println("一覧: " + new ReportPrinter(source).summarize(lines));
        System.out.println("最高点: " + new Ranking(source).topScorer(lines));
    }
}
