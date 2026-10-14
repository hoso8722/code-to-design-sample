package exam;

// 状態オブジェクトに、操作を任せる
public class Procedure {
    private State state = new Applied();

    public void take() { state = state.take(); }
    public void grade(int score) { state = state.grade(score); }
    public void announce() { state = state.announce(); }

    public String name() { return state.name(); }
    public String label() { return state.label(); }
}
