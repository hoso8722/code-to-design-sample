package exam;

import java.util.List;

// 手で書いた記録用のプロキシ：呼び出しを記録して、本物へ渡す
public class LoggingRepository implements StudentRepository {
    private final StudentRepository real;
    public int calls;

    public LoggingRepository(StudentRepository real) { this.real = real; }

    @Override
    public Student find(String id) {
        calls++;
        return real.find(id);
    }

    @Override
    public List<String> ids() {
        calls++;
        return real.ids();
    }
}
