package exam;

public record Practical(String name, int points) implements Subject {
    @Override
    public <R> R accept(SubjectVisitor<R> visitor) { return visitor.visitPractical(this); }
}
