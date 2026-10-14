// 検証しない: 複数ファイルの例。collect.sh で java switching/Switching.java として実行している
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

// 試験回ごとに、設定で判定ルールを切り替える。Session のコードは、ルールの種類を知らない
public class Switching {
    record Student(String name, int math, int english, int science) {
        int total() { return math + english + science; }
    }

    // ルールを受け取って使うだけのクラス
    record Session(String label, Predicate<Student> rule) {
        long countPassed(List<Student> students) {
            return students.stream().filter(rule).count();
        }
    }

    public static void main(String[] args) {
        Map<String, Predicate<Student>> catalog = new LinkedHashMap<>();
        catalog.put("general", s -> s.total() >= 180);
        catalog.put("transfer", s -> s.math() + s.english() >= 120);
        catalog.put("easy", s -> s.total() >= 150);   // コードの他の部分を変えずに、足したルール

        List<Student> students = List.of(
            new Student("佐藤", 60, 70, 80), new Student("鈴木", 90, 50, 40),
            new Student("高橋", 70, 70, 50), new Student("伊藤", 55, 60, 55));

        // 設定（ここでは文字列）から、試験回ごとのルールを決める
        String[] settings = {"春:general", "夏:transfer", "秋:easy"};
        for (String setting : settings) {
            String[] parts = setting.split(":");
            Session session = new Session(parts[0], catalog.get(parts[1]));
            System.out.println(session.label() + "（" + parts[1] + "）: 合格 " + session.countPassed(students) + " 人");
        }
    }
}
