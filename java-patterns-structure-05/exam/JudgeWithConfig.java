package exam;

// 設定を受け取る判定（依存がコンストラクタに見える）
public class JudgeWithConfig {
    private final Config config;

    public JudgeWithConfig(Config config) { this.config = config; }

    public boolean passes(int total) {
        return total >= config.passLine();
    }
}
