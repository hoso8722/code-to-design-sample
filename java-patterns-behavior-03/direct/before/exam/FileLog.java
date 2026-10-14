package exam;

public class FileLog {
    public void append(Result r) { System.out.println("[ログ] " + r.name() + "," + r.total() + "," + r.passed()); }
}
