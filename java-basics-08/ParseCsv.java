// 実行: java ParseCsv.java
public class ParseCsv {
    public static void main(String[] args) {
        String line = "田中,75,A";
        String[] parts = line.split(",");
        String name = parts[0];
        int score = Integer.parseInt(parts[1]);
        String grade = parts[2];
        System.out.println("名前: " + name);
        System.out.println("点数: " + score + "、次の試験で10点上がると: " + (score + 10));
        System.out.println("成績: " + grade);
    }
}
