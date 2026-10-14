package exam;

import java.util.List;

public class Ranking {
    public String topScorer(List<String> lines) {
        String best = null;
        int bestTotal = -1;
        for (String line : lines) {
            String[] c = line.split(",");
                    String name = c[4];
                    int science = Integer.parseInt(c[1]);
                    int math = Integer.parseInt(c[2]);
                    int english = Integer.parseInt(c[3]);
            int total = math + english + science;
            if (total > bestTotal) {
                bestTotal = total;
                best = name;
            }
        }
        return best;
    }
}
