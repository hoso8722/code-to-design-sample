package exam;

public class Stats implements ResultListener {
    private int passed, failed;

    @Override public void onResult(Result r) {
        if (r.passed()) passed++; else failed++;
    }

    public String summary() { return "合格 " + passed + " 人、不合格 " + failed + " 人"; }
}
