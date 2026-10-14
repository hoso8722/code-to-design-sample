package exam;

public class CsvImporter {
    public String importLine(String line) {
        String[] cols = line.split(",");
        String type = cols[0];
        Student student = new Student(cols[1], Integer.parseInt(cols[2]), Integer.parseInt(cols[3]), Integer.parseInt(cols[4]));
        PassingRule rule;
        if (type.equals("general")) {
            rule = new GeneralRule();
        } else if (type.equals("scholarship")) {
            rule = new ScholarshipRule();
        } else if (type.equals("transfer")) {
            rule = new TransferRule();
        } else {
            throw new IllegalArgumentException("unknown type: " + type);
        }
        return student.name() + ": " + (rule.passes(student) ? "PASS" : "FAIL");
    }
}
