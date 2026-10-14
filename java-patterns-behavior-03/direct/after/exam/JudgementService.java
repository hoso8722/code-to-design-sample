package exam;

public class JudgementService {
    private final ScreenBoard board = new ScreenBoard();
    private final Stats stats = new Stats();
    private final FileLog log = new FileLog();
private final AuditLog audit = new AuditLog();

    public void judge(String name, int total) {
        Result result = new Result(name, total, total >= 180);
        board.show(result);
        stats.add(result);
        log.append(result);
    audit.record(result);
    }

    public String summary() { return stats.summary(); }
}
