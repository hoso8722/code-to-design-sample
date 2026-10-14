package exam;

public class AuditLog implements ResultListener {
    @Override public void onResult(Result r) { if (!r.passed()) System.out.println("[監査] 不合格: " + r.name()); }
}
