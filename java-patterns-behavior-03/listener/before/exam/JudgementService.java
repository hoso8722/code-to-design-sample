package exam;

import java.util.ArrayList;
import java.util.List;

public class JudgementService {
    private final List<ResultListener> listeners = new ArrayList<>();
    private Stats stats;

    public void addListener(ResultListener listener) {
        listeners.add(listener);
        if (listener instanceof Stats s) stats = s;
    }

    public void judge(String name, int total) {
        Result result = new Result(name, total, total >= 180);
        for (ResultListener listener : listeners) {
            listener.onResult(result);
        }
    }

    public String summary() { return stats.summary(); }
}
