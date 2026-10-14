package exam;

// 遅延初期化（getInstance を synchronized にした）
public class SyncLazy {
    private static SyncLazy instance;
    private final int passLine;

    private SyncLazy() {
        this.passLine = Work.loadPassLine();
    }

    public static synchronized SyncLazy getInstance() {
        if (instance == null) {
            instance = new SyncLazy();
        }
        return instance;
    }

    public int passLine() { return passLine; }
}
