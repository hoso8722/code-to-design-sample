package exam;

import java.util.List;
import java.util.Map;

// 本物：作るときにデータベースへ接続する（300ミリ秒かかる、という想定）
public class DatabaseRepository implements StudentRepository {
    private static final Map<String, Student> DATA = Map.of(
        "s1", new Student("s1", "佐藤", 210),
        "s2", new Student("s2", "鈴木", 180),
        "s3", new Student("s3", "高橋", 190));

    public DatabaseRepository() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Clock.log("データベースへ接続した");
    }

    @Override
    public Student find(String id) { return DATA.get(id); }

    @Override
    public List<String> ids() { return List.of("s1", "s2", "s3"); }
}
