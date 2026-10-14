package exam;

// 手順6：分けすぎた版（動きは変えないが、読むために追う場所が増える）
public class Step6 {
    public static int passLine = 180;
    public static boolean verbose = false;

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private static final int SCHOLARSHIP_AVERAGE_LINE = 70;
    private static final int SCHOLARSHIP_SUBJECT_LINE = 60;
    private static final int TRANSFER_LINE = 120;

    private static boolean isValidName(String name) {
        return name != null && !name.isEmpty();
    }

    private static boolean isValidScore(int score) {
        return score >= MIN_SCORE && score <= MAX_SCORE;
    }

    private static boolean areValidScores(int math, int english, int science) {
        return isValidScore(math) && isValidScore(english) && isValidScore(science);
    }

    private static boolean isValidInput(String name, int math, int english, int science) {
        return isValidName(name) && areValidScores(math, english, science);
    }

    private static String toResult(boolean passed) {
        return passed ? "PASS" : "FAIL";
    }

    private static int total(int math, int english, int science) {
        return math + english + science;
    }

    private static int average(int math, int english, int science) {
        return total(math, english, science) / 3;
    }

    private static boolean reachesPassLine(int total) {
        return total >= passLine;
    }

    private static boolean reachesAverageLine(int average) {
        return average >= SCHOLARSHIP_AVERAGE_LINE;
    }

    private static boolean reachesSubjectLine(int score) {
        return score >= SCHOLARSHIP_SUBJECT_LINE;
    }

    private static boolean reachesAllSubjectLines(int math, int english, int science) {
        return reachesSubjectLine(math) && reachesSubjectLine(english) && reachesSubjectLine(science);
    }

    private static boolean reachesTransferLine(int math, int english) {
        return math + english >= TRANSFER_LINE;
    }

    private static boolean passesGeneral(int math, int english, int science) {
        return reachesPassLine(total(math, english, science));
    }

    private static boolean passesScholarship(int math, int english, int science) {
        return reachesAverageLine(average(math, english, science)) && reachesAllSubjectLines(math, english, science);
    }

    private static boolean passesTransfer(int math, int english) {
        return reachesTransferLine(math, english);
    }

    private static String judgeByType(String type, int math, int english, int science) {
        return switch (type) {
            case "general" -> toResult(passesGeneral(math, english, science));
            case "scholarship" -> toResult(passesScholarship(math, english, science));
            case "transfer" -> toResult(passesTransfer(math, english));
            default -> "INVALID";
        };
    }

    private static void log(String name, String type, int math, int english, int science, String result) {
        if (verbose) {
            System.out.println(name + " (" + type + "): " + math + "/" + english + "/" + science + " -> " + result);
        }
    }

    public static String judge(String name, int math, int english, int science, String type) {
        if (!isValidInput(name, math, english, science)) {
            return "INVALID";
        }
        String result = judgeByType(type, math, english, science);
        if (!result.equals("INVALID")) {
            log(name, type, math, english, science, result);
        }
        return result;
    }
}
