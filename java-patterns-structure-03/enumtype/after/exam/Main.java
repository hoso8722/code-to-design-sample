package exam;

public class Main {
    public static void main(String[] args) {
        System.out.println(new ReportPrinter().describe("scholarship"));
        System.out.println(new ExamService().judge("scholarship", Samples.HANAKO));
        for (String line : Samples.LINES) System.out.println(new CsvImporter().importLine(line));
    }
}
