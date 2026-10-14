package exam;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

// 継承版：外部のクラスを継承して、Candidate を実装する
public class ClassAdapter extends ExternalApplicant implements Candidate {
    public ClassAdapter(String fullName, Map<String, Integer> scores, Date birthDate) {
        super(fullName, scores, birthDate);
    }

    @Override public String name() { return getFullName(); }
    @Override public int math() { return getScore("math"); }
    @Override public int english() { return getScore("english"); }
    @Override public int science() { return getScore("science"); }
    @Override public LocalDate birthDate() {
        return getBirthDate().toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDate();
    }
}
