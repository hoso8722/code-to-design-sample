// 検証しない: パッケージつきの複数ファイルの例のため。実行結果は output-package.txt を参照
package school.app;

import school.model.Student;
import school.util.Grades;
import school.util.Stats;

public class Main {
    public static void main(String[] args) {
        Student tanaka = new Student("田中", 75);
        System.out.println(tanaka.name() + " の合否: " + Grades.isPassed(tanaka));

        // import を使わず、完全修飾名でも書ける
        school.model.Student suzuki = new school.model.Student("鈴木", 55);
        System.out.println(suzuki.name() + " の合否: " + Grades.isPassed(suzuki));

        int[] scores = {tanaka.score(), suzuki.score()};
        System.out.println("平均点: " + Stats.average(scores));
    }
}
