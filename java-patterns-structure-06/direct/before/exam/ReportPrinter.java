package exam;

import java.util.List;

public class ReportPrinter {
    public String summarize(List<String> lines) {
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String[] c = line.split(",");
                    String name = c[0];
                    int math = Integer.parseInt(c[1]);
                    int english = Integer.parseInt(c[2]);
                    int science = Integer.parseInt(c[3]);
            out.append(name).append("=").append(math + english + science).append(" ");
        }
        return out.toString().trim();
    }
}
