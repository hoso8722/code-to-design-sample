package exam;

// 基本の判定。実際のアプリケーションでは、外部の設定やデータベースを参照して、時間がかかる想定
public class ScoreJudge implements Judge {
    @Override
    public boolean passes(Student student) {
        return Work.slowTotal(student) >= 180;
    }
}
