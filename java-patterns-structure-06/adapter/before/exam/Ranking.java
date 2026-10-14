package exam;

import java.util.Comparator;
import java.util.List;

public class Ranking {
    private final StudentSource source;

    public Ranking(StudentSource source) { this.source = source; }

    public String topScorer(List<String> lines) {
        return lines.stream().map(source::parse).max(Comparator.comparingInt(Student::total)).map(Student::name).orElse(null);
    }
}
