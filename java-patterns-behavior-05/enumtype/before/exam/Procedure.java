package exam;

public class Procedure {
    private State state = State.APPLIED;
    private int score;

    public void take() { state = state.take(); }
    public void grade(int score) { state = state.grade(score); this.score = score; }
    public void announce() { state = state.announce(); }

    public String name() { return state.name(); }

    public String label() {
        return state == State.ANNOUNCED ? state.label() + "（" + score + "点）" : state.label();
    }
}
