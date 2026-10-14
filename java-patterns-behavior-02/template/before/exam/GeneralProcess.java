package exam;

public class GeneralProcess extends ExamProcess {
    @Override protected boolean judge(Student s) { return s.total() >= 180; }
    @Override protected String label() { return "一般"; }
}
