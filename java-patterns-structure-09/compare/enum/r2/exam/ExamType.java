package exam;

import java.util.function.Predicate;

public enum ExamType {
    GENERAL(s -> s.total() >= 180),
    SCHOLARSHIP(s -> s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60),
    RECOMMENDATION(s -> s.total() >= 150 && s.english() >= 70),
    TRANSFER(s -> s.math() + s.english() >= 120);

    private final Predicate<Student> rule;

    ExamType(Predicate<Student> rule) { this.rule = rule; }

    public boolean passes(Student s) { return rule.test(s); }

    public static ExamType from(String text) { return valueOf(text.toUpperCase()); }
}
