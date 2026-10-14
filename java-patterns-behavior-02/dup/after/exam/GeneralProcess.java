package exam;

import java.util.List;

public class GeneralProcess {
    public String run(List<String> lines) {
        StringBuilder out = new StringBuilder("【一般】");
        for (String line : lines) {
            // 1. 読み込む
            String[] c = line.split(",");
            if (c.length != 4) {
                out.append(" 不正な行");
                continue;
            }
            Student s = new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
        System.out.println("[審査ログ] " + s.name());
            // 2. 判定する
            boolean passed = s.total() >= 180;
            // 3. 出力する
            out.append(" ").append(s.name()).append(passed ? ":合格" : ":不合格");
        }
        return out.toString();
    }
}
