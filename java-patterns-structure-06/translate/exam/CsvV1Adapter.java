package exam;

// 旧形式：名前,数学,英語,理科。不正な行は、内側の例外に翻訳して投げる
public class CsvV1Adapter implements StudentSource {
    @Override
    public Student parse(String line) {
        try {
            String[] c = line.split(",");
            return new Student(c[0], Integer.parseInt(c[1]), Integer.parseInt(c[2]), Integer.parseInt(c[3]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new InvalidRecordException("不正な行: \"" + line + "\"", e);
        }
    }
}
