package exam;

import java.util.HashMap;
import java.util.Map;

public class CachingJudge implements Judge {
    private final Judge inner;
    private final Map<String, Boolean> cache = new HashMap<>();

    public CachingJudge(Judge inner) { this.inner = inner; }

    @Override
    public boolean passes(Student student) {
        Boolean hit = cache.get(student.name());
        if (hit != null) return hit;
        boolean result = inner.passes(student);
        cache.put(student.name(), result);
        return result;
    }
}
