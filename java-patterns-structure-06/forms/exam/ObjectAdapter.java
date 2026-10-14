package exam;

import java.time.LocalDate;
import java.time.ZoneId;

// 委譲版：外部のオブジェクトを持ち、Candidate を実装する
public class ObjectAdapter implements Candidate {
    private final ExternalApplicant applicant;

    public ObjectAdapter(ExternalApplicant applicant) { this.applicant = applicant; }

    @Override public String name() { return applicant.getFullName(); }
    @Override public int math() { return applicant.getScore("math"); }
    @Override public int english() { return applicant.getScore("english"); }
    @Override public int science() { return applicant.getScore("science"); }
    @Override public LocalDate birthDate() {
        return applicant.getBirthDate().toInstant().atZone(ZoneId.of("Asia/Tokyo")).toLocalDate();
    }
}
