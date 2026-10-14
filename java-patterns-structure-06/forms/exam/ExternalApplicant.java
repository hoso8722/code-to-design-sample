package exam;

import java.util.Date;
import java.util.Map;

// 外部のライブラリのクラスを想定している（変更できない）
public class ExternalApplicant {
    private final String fullName;
    private final Map<String, Integer> scores;
    private final Date birthDate;

    public ExternalApplicant(String fullName, Map<String, Integer> scores, Date birthDate) {
        this.fullName = fullName;
        this.scores = scores;
        this.birthDate = birthDate;
    }

    public String getFullName() { return fullName; }
    public int getScore(String subject) { return scores.getOrDefault(subject, 0); }
    public Date getBirthDate() { return birthDate; }
}
