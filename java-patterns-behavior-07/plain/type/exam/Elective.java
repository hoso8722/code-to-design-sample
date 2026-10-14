package exam;

public class Elective extends Subject {
    public Elective(String name, int points) { super(name, points); }

    @Override int score() { return points / 2; }
    @Override String describe() { return "選択科目「" + name + "」" + points + "点（配点50%）"; }
    @Override boolean valid() { return points >= 0 && points <= 100; }
}
