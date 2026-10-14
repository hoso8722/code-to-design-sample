package exam;

public class Required extends Subject {
    public Required(String name, int points) { super(name, points); }

    @Override int score() { return points; }
    @Override String describe() { return "必須科目「" + name + "」" + points + "点（配点100%）"; }
    @Override boolean valid() { return points >= 0 && points <= 100; }
    @Override int maxPoints() { return 100 * 100 / 100; }
}
