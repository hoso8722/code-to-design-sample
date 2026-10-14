package exam;

public class TimingJudge implements Judge {
    private final Judge inner;
    public long nanos;

    public TimingJudge(Judge inner) { this.inner = inner; }

    @Override
    public boolean passes(Student student) {
        long start = System.nanoTime();
        boolean result = inner.passes(student);
        nanos += System.nanoTime() - start;
        return result;
    }
}
