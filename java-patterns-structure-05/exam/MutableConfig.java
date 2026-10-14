package exam;

// 合格点を変更できる Singleton（アプリケーション全体で、1つの設定を共有する）
public class MutableConfig {
    private static final MutableConfig INSTANCE = new MutableConfig();
    private int passLine = 180;

    private MutableConfig() {}

    public static MutableConfig getInstance() { return INSTANCE; }

    public int passLine() { return passLine; }

    public void setPassLine(int passLine) { this.passLine = passLine; }
}
