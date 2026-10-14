package exam;

public interface PassingRule {
    boolean passes(Student student);
    String describe();
}
