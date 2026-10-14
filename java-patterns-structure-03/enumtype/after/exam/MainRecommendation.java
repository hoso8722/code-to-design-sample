package exam;

// 追加した入試区分「推薦」を使う
public class MainRecommendation {
    public static void main(String[] args) {
        System.out.println(new ReportPrinter().describe("recommendation"));
        System.out.println(new ExamService().judge("recommendation", new Student("山田", 50, 70, 40)));
        System.out.println(new CsvImporter().importLine("recommendation,山田,50,70,40"));
    }
}
