package exam;

public class ValidVisitor implements SubjectVisitor<Boolean> {
    @Override public Boolean visitRequired(Required s) { return s.points() >= 0 && s.points() <= 100; }
    @Override public Boolean visitElective(Elective s) { return s.points() >= 0 && s.points() <= 100; }
    @Override public Boolean visitPractical(Practical s) { return s.points() >= 0 && s.points() <= 100; }
}
