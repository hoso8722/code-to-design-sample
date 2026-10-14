package exam;

// sealed interface と record：状態と操作（イベント）を型で表し、遷移を switch で書く
public class Procedure {
    sealed interface State permits Applied, Taken, Grading, Announced {}
    record Applied() implements State {}
    record Taken() implements State {}
    record Grading(int score) implements State {}
    record Announced(int score) implements State {}

    sealed interface Event permits Take, Grade, Announce {}
    record Take() implements Event {}
    record Grade(int score) implements Event {}
    record Announce() implements Event {}

    private State state = new Applied();

    private static State next(State state, Event event) {
        return switch (state) {
            case Applied a -> switch (event) {
                case Take t -> new Taken();
                default -> throw new IllegalStateException(name(state) + " では" + event.getClass().getSimpleName() + "できません");
            };
            case Taken t -> switch (event) {
                case Grade g -> new Grading(g.score());
                default -> throw new IllegalStateException(name(state) + " では" + event.getClass().getSimpleName() + "できません");
            };
            case Grading g -> switch (event) {
                case Announce a -> new Announced(g.score());
                default -> throw new IllegalStateException(name(state) + " では" + event.getClass().getSimpleName() + "できません");
            };
            case Announced a -> throw new IllegalStateException(name(state) + " では" + event.getClass().getSimpleName() + "できません");
        };
    }

    public void take() { state = next(state, new Take()); }
    public void grade(int score) { state = next(state, new Grade(score)); }
    public void announce() { state = next(state, new Announce()); }

    private static String name(State state) {
        return switch (state) {
            case Applied a -> "APPLIED";
            case Taken t -> "TAKEN";
            case Grading g -> "GRADING";
            case Announced a -> "ANNOUNCED";
        };
    }

    public String name() { return name(state); }

    public String label() {
        return switch (state) {
            case Applied a -> "申込済み";
            case Taken t -> "受験済み";
            case Grading g -> "採点中";
            case Announced a -> "結果発表済み（" + a.score() + "点）";
        };
    }
}
