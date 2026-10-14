package exam;

import java.util.List;

public interface StudentRepository {
    Student find(String id);
    List<String> ids();
}
