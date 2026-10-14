package exam;

import java.util.List;

public class ExamService {
    public long countPassed(List<String> lines) {
        long passed = 0;
        for (String line : lines) {
            String[] c = line.split(",");
            String name = c[0];
            int math = Integer.parseInt(c[1]);
            int english = Integer.parseInt(c[2]);
            int science = Integer.parseInt(c[3]);
            if (math + english + science >= 180) passed++;
        }
        return passed;
    }
}
