package exam;

import java.util.List;

// Template Method：流れ（run）は親クラスに固定し、変わる手順（judge、label）だけを子クラスで決める
public abstract class ExamProcess {
    protected abstract boolean judge(Student s);

    protected abstract String label();

    public final String run(List<String> lines) {
        StringBuilder out = new StringBuilder("【" + label() + "】");
        for (String line : lines) {
            String[] c = line.split(",");
            if (c.length != 4) {
                out.append(" 不正な行");
                continue;
            }
            Student s = new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
            out.append(" ").append(s.name()).append(judge(s) ? ":合格" : ":不合格");
        }
        return out.toString();
    }
}
