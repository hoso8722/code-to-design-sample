package exam;

public class ScreenBoard {
    public void show(Result r) { System.out.println("[画面] " + r.name() + " " + (r.passed() ? "合格" : "不合格")); }
}
