package exam;

public class TimingCachingJudge extends ScoreJudge {
    private final java.util.Map<String, Boolean> cache = new java.util.HashMap<>();
    public long nanos;
    @Override
    public boolean passes(Student student) {
        Boolean hit = cache.get(student.name());
        if (hit != null) return hit;
        long start = System.nanoTime();
        boolean result = super.passes(student);
        nanos += System.nanoTime() - start;
        cache.put(student.name(), result);
        return result;
    }
}
