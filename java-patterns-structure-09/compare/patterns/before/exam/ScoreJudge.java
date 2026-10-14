package exam;

public class ScoreJudge implements Judge {
    @Override
    public boolean passes(String type, Student student) {
        return PassingRules.of(type).passes(student);
    }
}
