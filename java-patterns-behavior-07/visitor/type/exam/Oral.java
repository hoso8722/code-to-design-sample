package exam;

public record Oral(String name, int points) implements Subject {
    @Override
    public <R> R accept(SubjectVisitor<R> visitor) { return visitor.visitOral(this); }
}
