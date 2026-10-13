// 実行: java Report.java
void main() {
    String name = "田中";
    int score = 75;
    String report = """
        学生: %s
        点数: %d
        判定: %s
        """.formatted(name, score, score >= 60 ? "合格" : "不合格");
    IO.print(report);
}
