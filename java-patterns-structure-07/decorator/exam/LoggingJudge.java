package exam;

public class LoggingJudge implements Judge {
    private final Judge inner;

    public LoggingJudge(Judge inner) { this.inner = inner; }

    @Override
    public boolean passes(Student student) {
        boolean result = inner.passes(student);
        System.out.println("判定: " + student.name() + " -> " + result);
        return result;
    }
}
