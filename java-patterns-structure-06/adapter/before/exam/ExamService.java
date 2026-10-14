package exam;

import java.util.List;

public class ExamService {
    private final StudentSource source;

    public ExamService(StudentSource source) { this.source = source; }

    public long countPassed(List<String> lines) {
        return lines.stream().map(source::parse).filter(s -> s.total() >= 180).count();
    }
}
