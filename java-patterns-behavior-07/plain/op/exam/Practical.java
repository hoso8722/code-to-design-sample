package exam;

public class Practical extends Subject {
    public Practical(String name, int points) { super(name, points); }

    @Override int score() { return points * 8 / 10; }
    @Override String describe() { return "実技科目「" + name + "」" + points + "点（配点80%）"; }
    @Override boolean valid() { return points >= 0 && points <= 100; }
    @Override int maxPoints() { return 100 * 80 / 100; }
}
