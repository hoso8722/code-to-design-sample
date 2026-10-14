package exam;

public class ScoreVisitor implements SubjectVisitor<Integer> {
    @Override public Integer visitRequired(Required s) { return s.points(); }
    @Override public Integer visitElective(Elective s) { return s.points() / 2; }
    @Override public Integer visitPractical(Practical s) { return s.points() * 8 / 10; }
    @Override public Integer visitOral(Oral s) { return s.points() * 6 / 10; }
}
