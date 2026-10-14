package exam;

import java.util.Map;
import java.util.TreeMap;

// 受験者の成績を持つ帳簿
public class ScoreBook {
    private final Map<String, Student> students = new TreeMap<>();

    public Student get(String name) { return students.get(name); }
    public void put(Student student) { students.put(student.name(), student); }
    public Student remove(String name) { return students.remove(name); }
    public Map<String, Student> snapshot() { return new TreeMap<>(students); }
    public void restore(Map<String, Student> snapshot) {
        students.clear();
        students.putAll(snapshot);
    }

    @Override
    public String toString() { return students.values().toString(); }
}
