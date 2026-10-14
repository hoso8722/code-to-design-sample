package exam;

public interface StudentSource {
    Entry parse(String line);

    record Entry(String type, Student student) {}
}
