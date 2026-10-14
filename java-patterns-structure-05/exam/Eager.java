package exam;

// 先に作る（クラスが読み込まれて初期化されるときに、1回だけ作られる）
public class Eager {
    private static final Eager INSTANCE = new Eager();
    private final int passLine;

    private Eager() {
        this.passLine = Work.loadPassLine();
    }

    public static Eager getInstance() { return INSTANCE; }

    public int passLine() { return passLine; }
}
