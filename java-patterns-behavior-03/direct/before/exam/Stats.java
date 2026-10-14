package exam;

public class Stats {
    private int passed, failed;

    public void add(Result r) {
        if (r.passed()) passed++; else failed++;
    }

    public String summary() { return "合格 " + passed + " 人、不合格 " + failed + " 人"; }
}
