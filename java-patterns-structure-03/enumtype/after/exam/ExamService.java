package exam;

public class ExamService {
    public boolean judge(String type, Student student) {
        return ExamType.from(type).rule().passes(student);
    }
}
