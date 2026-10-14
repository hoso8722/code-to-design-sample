package exam;

// 新形式：ID,理科,数学,英語,名前
public class CsvV2Adapter implements StudentSource {
    @Override
    public Student parse(String line) {
        String[] c = line.split(",");
        return new Student(c[4], Integer.parseInt(c[2]), Integer.parseInt(c[3]), Integer.parseInt(c[1]));
    }
}
