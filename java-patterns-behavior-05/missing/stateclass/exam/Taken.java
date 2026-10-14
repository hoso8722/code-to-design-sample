package exam;

public class Taken implements State {
    @Override public State grade(int score) { return new Grading(score); }
    @Override public State cancel() { return new Cancelled(); }
    @Override public String name() { return "TAKEN"; }
    @Override public String label() { return "受験済み"; }
}
