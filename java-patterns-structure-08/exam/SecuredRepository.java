package exam;

import java.util.List;

// 保護プロキシ：権限のない呼び出しを、本物へ渡さない
public class SecuredRepository implements StudentRepository {
    public enum Role { STAFF, GUEST }

    private final StudentRepository real;
    private final Role role;

    public SecuredRepository(StudentRepository real, Role role) {
        this.real = real;
        this.role = role;
    }

    @Override
    public Student find(String id) {
        if (role != Role.STAFF) throw new SecurityException("権限がありません: " + role);
        return real.find(id);
    }

    @Override
    public List<String> ids() {
        if (role != Role.STAFF) throw new SecurityException("権限がありません: " + role);
        return real.ids();
    }
}
