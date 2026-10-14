package exam;

// holder idiom：内部クラスが読み込まれたときに作る（遅延初期化で、同期の記述も要らない）
public class Holder {
    private final int passLine;

    private Holder() {
        this.passLine = Work.loadPassLine();
    }

    private static class Lazy {
        static final Holder INSTANCE = new Holder();
    }

    public static Holder getInstance() { return Lazy.INSTANCE; }

    public int passLine() { return passLine; }
}
