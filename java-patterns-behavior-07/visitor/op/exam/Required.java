package exam;

public record Required(String name, int points) implements Subject {
    @Override
    public <R> R accept(SubjectVisitor<R> visitor) { return visitor.visitRequired(this); }
}
