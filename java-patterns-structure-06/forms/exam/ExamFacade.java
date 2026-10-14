package exam;

import java.util.List;

// Facade：複数の部品を呼ぶ手順を、1つの簡単な入口にまとめる（形は変えない）
public class ExamFacade {
    public String report(List<Candidate> candidates) {
        long passed = candidates.stream().filter(c -> c.total() >= 180).count();
        String top = candidates.stream().max(java.util.Comparator.comparingInt(Candidate::total)).map(Candidate::name).orElse("-");
        return "合格 " + passed + " 人、最高点 " + top;
    }
}
