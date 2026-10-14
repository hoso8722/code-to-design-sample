package exam;

import java.util.List;

public class App {
    public static void main(String[] args) {
        List<String> lines = List.of("101,80,60,70,佐藤", "102,40,90,50,鈴木", "103,50,70,70,高橋");
        System.out.println("合格: " + new ExamService().countPassed(lines));
        System.out.println("一覧: " + new ReportPrinter().summarize(lines));
        System.out.println("最高点: " + new Ranking().topScorer(lines));
    }
}
