package exam;

// Singleton を直接呼ぶ判定（依存が見えない）
public class JudgeWithSingleton {
    public boolean passes(int total) {
        return total >= MutableConfig.getInstance().passLine();
    }
}
