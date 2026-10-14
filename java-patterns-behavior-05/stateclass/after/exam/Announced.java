package exam;

public class Announced implements State {
    private final int score;

    public Announced(int score) { this.score = score; }

    @Override public String name() { return "ANNOUNCED"; }
    @Override public String label() { return "結果発表済み（" + score + "点）"; }
}
