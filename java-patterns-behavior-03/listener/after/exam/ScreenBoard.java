package exam;

public class ScreenBoard implements ResultListener {
    @Override public void onResult(Result r) { System.out.println("[画面] " + r.name() + " " + (r.passed() ? "合格" : "不合格")); }
}
