package exam;

@FunctionalInterface
public interface PassingRule {
    boolean passes(Student student);
}
