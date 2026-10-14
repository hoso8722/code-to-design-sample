package exam;

public class Grading implements State {
    private final int score;

    public Grading(int score) { this.score = score; }

    @Override public State announce() { return new Announced(score); }
    @Override public String name() { return "GRADING"; }
    @Override public String label() { return "採点中"; }
}
