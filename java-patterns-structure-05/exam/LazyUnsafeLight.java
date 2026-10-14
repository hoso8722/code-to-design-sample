package exam;

// 遅延初期化（同期なし）で、コンストラクタがすぐ終わる版
public class LazyUnsafeLight {
    private static LazyUnsafeLight instance;
    private final int passLine;

    private LazyUnsafeLight() {
        this.passLine = 180;   // 設定を読み込まず、固定の値を入れるだけ
    }

    public static LazyUnsafeLight getInstance() {
        if (instance == null) {
            instance = new LazyUnsafeLight();
        }
        return instance;
    }

    public int passLine() { return passLine; }
}
