package exam;

public class LoggingJudge implements Judge {
    private final Judge inner;

    public LoggingJudge(Judge inner) { this.inner = inner; }

    @Override
    public boolean passes(String type, Student s) {
        boolean r = inner.passes(type, s);
        System.out.println(s.name() + " (" + type + ") -> " + (r ? "PASS" : "FAIL"));
        return r;
    }
}
