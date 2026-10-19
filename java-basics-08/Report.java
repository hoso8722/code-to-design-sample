// 実行: java Report.java
public class Report {
    public static void main(String[] args) {
        String name = "田中";
        int score = 75;
        String report = """
            学生: %s
            点数: %d
            判定: %s
            """.formatted(name, score, score >= 60 ? "合格" : "不合格");
        System.out.print(report);
    }
}
