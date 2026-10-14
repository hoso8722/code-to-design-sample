package exam;

public class LoggingTimingJudge extends ScoreJudge {
    public long nanos;
    @Override
    public boolean passes(Student student) {
        long start = System.nanoTime();
        boolean result = super.passes(student);
        nanos += System.nanoTime() - start;
        System.out.println("判定: " + student.name() + " -> " + result);
        return result;
    }
}
