package exam;

public class Oral extends Subject {
    public Oral(String name, int points) { super(name, points); }

    @Override int score() { return points * 6 / 10; }
    @Override String describe() { return "口述科目「" + name + "」" + points + "点（配点60%）"; }
    @Override boolean valid() { return points >= 0 && points <= 100; }
}
