package school.util;

import school.model.Student;

public class Grades {
    public static final int PASS_SCORE = 60;

    public static boolean isPassed(Student student) {
        return student.score() >= PASS_SCORE;
    }
}
