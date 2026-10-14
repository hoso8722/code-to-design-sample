package exam;

public class MaxVisitor implements SubjectVisitor<Integer> {
    @Override public Integer visitRequired(Required s) { return 100 * 100 / 100; }
    @Override public Integer visitElective(Elective s) { return 100 * 50 / 100; }
    @Override public Integer visitPractical(Practical s) { return 100 * 80 / 100; }
}
