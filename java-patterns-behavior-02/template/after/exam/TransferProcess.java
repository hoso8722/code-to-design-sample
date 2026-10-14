package exam;

public class TransferProcess extends ExamProcess {
    @Override protected boolean judge(Student s) { return s.math() + s.english() >= 120; }
    @Override protected String label() { return "編入"; }
}
