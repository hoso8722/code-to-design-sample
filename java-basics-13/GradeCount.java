// 実行: java GradeCount.java
import java.util.Map;
import java.util.TreeMap;

public class GradeCount {
    public static void main(String[] args) {
        String[] grades = {"A", "C", "B", "A", "D", "C", "A"};

        Map<String, Integer> count = new TreeMap<>();     // キーの順に並ぶ Map
        for (String grade : grades) {
            count.merge(grade, 1, Integer::sum);          // なければ 1、あれば +1
        }
        System.out.println(count);
    }
}
