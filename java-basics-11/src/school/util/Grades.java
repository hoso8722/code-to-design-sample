// 検証しない: パッケージつきの複数ファイルの例のため。実行結果は output-package.txt を参照
package school.util;

import school.model.Student;

public class Grades {
    public static final int PASS_SCORE = 60;

    public static boolean isPassed(Student student) {
        return student.score() >= PASS_SCORE;
    }
}
