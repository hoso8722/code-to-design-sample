package exam;

public record Elective(String name, int points) implements Subject {
    @Override
    public <R> R accept(SubjectVisitor<R> visitor) { return visitor.visitElective(this); }
}
