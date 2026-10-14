package exam;

public interface SubjectVisitor<R> {
    R visitRequired(Required subject);
    R visitElective(Elective subject);
    R visitPractical(Practical subject);
    R visitOral(Oral subject);
}
