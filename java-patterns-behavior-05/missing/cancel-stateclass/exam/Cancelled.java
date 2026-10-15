package exam;

public class Cancelled implements State {
    @Override public String name() { return "CANCELLED"; }
    @Override public String label() { return "取り消し済み"; }
}
