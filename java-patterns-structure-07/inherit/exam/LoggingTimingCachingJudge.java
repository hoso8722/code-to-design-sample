package exam;

public class LoggingTimingCachingJudge extends ScoreJudge {
    private final java.util.Map<String, Boolean> cache = new java.util.HashMap<>();
    public long nanos;
    @Override
    public boolean passes(Student student) {
        Boolean hit = cache.get(student.name());
        if (hit != null) return hit;
        long start = System.nanoTime();
        boolean result = super.passes(student);
        nanos += System.nanoTime() - start;
        System.out.println("判定: " + student.name() + " -> " + result);
        cache.put(student.name(), result);
        return result;
    }
}
