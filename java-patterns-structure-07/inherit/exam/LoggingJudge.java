package exam;

public class LoggingJudge extends ScoreJudge {
    @Override
    public boolean passes(Student student) {
        boolean result = super.passes(student);
        System.out.println("判定: " + student.name() + " -> " + result);
        return result;
    }
}
