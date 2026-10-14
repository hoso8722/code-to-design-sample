package exam;

import java.time.LocalDate;

// 内側のコードが期待する形
public interface Candidate {
    String name();
    int math();
    int english();
    int science();
    LocalDate birthDate();

    default int total() { return math() + english() + science(); }
}
