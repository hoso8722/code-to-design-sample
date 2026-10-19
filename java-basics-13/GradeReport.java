// 実行: java GradeReport.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class GradeReport {
    // 「名前,点数」の形式の行を Student に変換する。形式が違う場合は例外を投げる
    static Student parse(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("列の数が違います: " + line);
        }
        int score = Integer.parseInt(parts[1].trim());
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("点数が範囲外です: " + line);
        }
        return new Student(parts[0].trim(), score);
    }

    public static void main(String[] args) {
        String[] lines = {"田中,75", "鈴木,55", "佐藤,95", "高橋,abc", "伊藤,105", "渡辺", "山田,82"};

        List<Student> students = new ArrayList<>();
        for (String line : lines) {
            try {
                students.add(parse(line));
            } catch (NumberFormatException e) {
                System.out.println("読み飛ばし(数値ではない): " + line);
            } catch (IllegalArgumentException e) {
                System.out.println("読み飛ばし(" + e.getMessage() + ")");
            }
        }

        // 集計
        int total = 0;
        int max = Integer.MIN_VALUE;
        String top = "";
        Map<String, Integer> gradeCount = new HashMap<>();
        List<String> failed = new ArrayList<>();
        for (Student student : students) {
            total += student.score();
            if (student.score() > max) {
                max = student.score();
                top = student.name();
            }
            gradeCount.merge(student.grade(), 1, Integer::sum);
            if (!student.isPassed()) {
                failed.add(student.name());
            }
        }

        System.out.println("読み込めた人数: " + students.size());
        System.out.println("平均点: " + String.format("%.1f", (double) total / students.size()));
        System.out.println("最高点: " + top + " " + max + "点");
        System.out.println("成績ごとの人数: " + new java.util.TreeMap<>(gradeCount));
        System.out.println("不合格: " + failed);
    }
}

record Student(String name, int score) {
    boolean isPassed() {
        return score >= 60;
    }

    String grade() {
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 60) return "C";
        return "D";
    }
}
