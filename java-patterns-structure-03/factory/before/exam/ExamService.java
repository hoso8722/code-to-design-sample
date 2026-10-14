package exam;

public class ExamService {
    public boolean judge(String type, Student student) {
        return PassingRules.of(type).passes(student);
    }
}
