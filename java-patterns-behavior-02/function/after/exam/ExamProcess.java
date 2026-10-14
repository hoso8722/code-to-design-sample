package exam;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

// 変わる手順を、コンストラクタで関数として受け取る
public class ExamProcess {
    private final String label;
    private final Predicate<Student> judge;
    private final Consumer<Student> audit;

    public ExamProcess(String label, Predicate<Student> judge) {
        this.label = label;
        this.judge = judge;
        this.audit = s -> System.out.println("[審査ログ] " + s.name());
    }

    public String run(List<String> lines) {
        StringBuilder out = new StringBuilder("【" + label + "】");
        for (String line : lines) {
            String[] c = line.split(",");
            if (c.length != 4) {
                out.append(" 不正な行");
                continue;
            }
            Student s = new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
            audit.accept(s);
            out.append(" ").append(s.name()).append(judge.test(s) ? ":合格" : ":不合格");
        }
        return out.toString();
    }
}
