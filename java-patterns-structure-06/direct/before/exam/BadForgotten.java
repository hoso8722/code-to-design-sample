// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class BadForgotten {
    public static void main(String[] args) {
        // 新形式（ID,理科,数学,英語,名前）のCSVを、直し忘れた旧形式の読み取りに渡す
        List<String> newFormat = List.of("101,80,60,70,佐藤", "102,40,90,50,鈴木", "103,50,70,70,高橋");
        System.out.println("合格: " + new ExamService().countPassed(newFormat));
        System.out.println("一覧: " + new ReportPrinter().summarize(newFormat));
        System.out.println("最高点: " + new Ranking().topScorer(newFormat));
    }
}
