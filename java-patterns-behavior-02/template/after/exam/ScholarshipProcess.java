package exam;

public class ScholarshipProcess extends ExamProcess {
    @Override protected boolean judge(Student s) { return s.total() / 3 >= 70 && s.math() >= 60 && s.english() >= 60 && s.science() >= 60; }
    @Override protected String label() { return "奨学"; }
}
