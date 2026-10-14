package exam;

public class CsvImporter {
    public String importLine(String line) {
        String[] cols = line.split(",");
        Student student = new Student(cols[1], Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4]));
        PassingRule rule = ExamType.from(cols[0]).rule();
        return student.name() + ": " + (rule.passes(student) ? "PASS" : "FAIL");
    }
}
