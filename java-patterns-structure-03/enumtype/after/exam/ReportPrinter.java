package exam;

public class ReportPrinter {
    public String describe(String type) {
        return ExamType.from(type).rule().describe();
    }
}
