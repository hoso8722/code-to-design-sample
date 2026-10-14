package exam;

// 遅延初期化（同期なし）：最初に呼ばれたときに作る
public class LazyUnsafe {
    private static LazyUnsafe instance;
    private final int passLine;

    private LazyUnsafe() {
        this.passLine = Work.loadPassLine();   // 設定を読み込む処理（少し時間がかかる）
    }

    public static LazyUnsafe getInstance() {
        if (instance == null) {
            instance = new LazyUnsafe();
        }
        return instance;
    }

    public int passLine() { return passLine; }
}
