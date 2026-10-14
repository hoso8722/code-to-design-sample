package exam;

public class CsvV1Adapter implements StudentSource {
    @Override
    public Entry parse(String line) {
        String[] c = line.split(",");
        return new Entry(c[0], new Student(c[1], Integer.parseInt(c[2]), Integer.parseInt(c[3]), Integer.parseInt(c[4])));
    }
}
