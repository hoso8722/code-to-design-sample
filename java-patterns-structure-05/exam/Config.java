package exam;

// 同じ設定を、Singleton にせず、必要な場所へ渡す形で持つ
public class Config {
    private int passLine;

    public Config(int passLine) { this.passLine = passLine; }

    public int passLine() { return passLine; }

    public void setPassLine(int passLine) { this.passLine = passLine; }
}
