// 合計点が合格ラインに達したかを判定する
public class PassingRule {
    private final int passLine;

    public PassingRule(int passLine) {
        this.passLine = passLine;
    }

    public boolean passes(int total) {
        return total >= passLine;
    }
}
