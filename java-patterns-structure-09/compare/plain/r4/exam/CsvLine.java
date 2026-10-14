package exam;

public class CsvLine {
    public record Entry(String type, Student student) {}

    public static Entry parse(String line) {
        String[] c = line.split(",");
        return new Entry(c[0], new Student(c[5], Integer.parseInt(c[3]), Integer.parseInt(c[4]), Integer.parseInt(c[2])));
    }
}
