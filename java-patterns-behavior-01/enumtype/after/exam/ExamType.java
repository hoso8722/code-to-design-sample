package exam;

import java.util.function.Predicate;

public enum ExamType {
    GENERAL(s -> s.total() >= 180, "合計180点以上"),
    SCHOLARSHIP(s -> s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60, "平均70点以上、各科目60点以上"),
    TRANSFER(s -> s.math() + s.english() >= 120, "数学と英語の合計120点以上"),
    RECOMMENDATION(s -> s.total() >= 150 && s.english() >= 70, "合計150点以上、英語70点以上");

    private final Predicate<Student> test;
    private final String explanation;

    ExamType(Predicate<Student> test, String explanation) {
        this.test = test;
        this.explanation = explanation;
    }

    public boolean passes(Student s) { return test.test(s); }

    public String explain() { return explanation; }
}
