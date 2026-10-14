package exam;

import java.util.List;
import java.util.function.Supplier;

// 仮想プロキシ：本物を、最初に使われるときまで作らない
public class LazyRepository implements StudentRepository {
    private final Supplier<StudentRepository> factory;
    private StudentRepository real;

    public LazyRepository(Supplier<StudentRepository> factory) { this.factory = factory; }

    private synchronized StudentRepository real() {
        if (real == null) {
            real = factory.get();
        }
        return real;
    }

    @Override
    public Student find(String id) { return real().find(id); }

    @Override
    public List<String> ids() { return real().ids(); }
}
