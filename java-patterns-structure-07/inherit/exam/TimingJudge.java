package exam;

public class TimingJudge extends ScoreJudge {
    public long nanos;
    @Override
    public boolean passes(Student student) {
        long start = System.nanoTime();
        boolean result = super.passes(student);
        nanos += System.nanoTime() - start;
        return result;
    }
}
