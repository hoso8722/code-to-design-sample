package exam;

public class Applied implements State {
    @Override public State take() { return new Taken(); }
    @Override public String name() { return "APPLIED"; }
    @Override public String label() { return "申込済み"; }
}
