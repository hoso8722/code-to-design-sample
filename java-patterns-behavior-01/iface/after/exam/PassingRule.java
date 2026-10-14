package exam;

public interface PassingRule {
    boolean passes(Student s);
    String explain();
}
