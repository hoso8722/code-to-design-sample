package exam;

// enum：要素を1つだけ持つ enum
public enum EnumConfig {
    INSTANCE;

    private final int passLine = Work.loadPassLine();

    public static EnumConfig getInstance() { return INSTANCE; }

    public int passLine() { return passLine; }
}
