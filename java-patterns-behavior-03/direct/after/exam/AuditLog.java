package exam;

public class AuditLog {
    public void record(Result r) { if (!r.passed()) System.out.println("[監査] 不合格: " + r.name()); }
}
