// 実行: java ParseCsv.java
void main() {
    String line = "田中,75,A";
    String[] parts = line.split(",");
    String name = parts[0];
    int score = Integer.parseInt(parts[1]);
    String grade = parts[2];
    IO.println("名前: " + name);
    IO.println("点数: " + score + "、次の試験で10点上がると: " + (score + 10));
    IO.println("成績: " + grade);
}
