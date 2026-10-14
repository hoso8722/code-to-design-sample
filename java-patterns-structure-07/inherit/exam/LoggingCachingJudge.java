package exam;

public class LoggingCachingJudge extends ScoreJudge {
    private final java.util.Map<String, Boolean> cache = new java.util.HashMap<>();
    @Override
    public boolean passes(Student student) {
        Boolean hit = cache.get(student.name());
        if (hit != null) return hit;
        boolean result = super.passes(student);
        System.out.println("判定: " + student.name() + " -> " + result);
        cache.put(student.name(), result);
        return result;
    }
}
