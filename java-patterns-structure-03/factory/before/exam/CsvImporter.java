package exam;

public class CsvImporter {
    public String importLine(String line) {
        String[] cols = line.split(",");
        Student student = new Student(cols[1], Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4]));
        PassingRule rule = PassingRules.of(cols[0]);
        return student.name() + ": " + (rule.passes(student) ? "PASS" : "FAIL");
    }
}
