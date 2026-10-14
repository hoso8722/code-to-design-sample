package exam;

public class FileLog implements ResultListener {
    @Override public void onResult(Result r) { System.out.println("[ログ] " + r.name() + "," + r.total() + "," + r.passed()); }
}
