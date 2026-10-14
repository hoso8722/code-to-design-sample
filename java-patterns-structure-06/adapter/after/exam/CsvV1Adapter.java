package exam;

// 旧形式：名前,数学,英語,理科
public class CsvV1Adapter implements StudentSource {
    @Override
    public Student parse(String line) {
        String[] c = line.split(",");
        return new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
    }
}
