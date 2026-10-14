package exam;

import java.util.List;

public class ReportPrinter {
    private final StudentSource source;

    public ReportPrinter(StudentSource source) { this.source = source; }

    public String summarize(List<String> lines) {
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            Student s = source.parse(line);
            out.append(s.name()).append("=").append(s.total()).append(" ");
        }
        return out.toString().trim();
    }
}
