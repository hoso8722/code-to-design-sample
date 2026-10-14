package exam;

// 手順5：条件分岐の連鎖を switch 式にし、表示を取り出す（動きは変えない）
public class Step5 {
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

    private static String toResult(boolean passed) {
        return passed ? "PASS" : "FAIL";
    }

    private static String judgeGeneral(int math, int english, int science) {
        return toResult(math + english + science >= passLine);
    }

    private static String judgeScholarship(int math, int english, int science) {
        int average = (math + english + science) / 3;
        return toResult(average >= SCHOLARSHIP_AVERAGE_LINE
                && math >= SCHOLARSHIP_SUBJECT_LINE
                && english >= SCHOLARSHIP_SUBJECT_LINE
                && science >= SCHOLARSHIP_SUBJECT_LINE);
    }

    private static String judgeTransfer(int math, int english) {
        return toResult(math + english >= TRANSFER_LINE);
    }

    private static void log(String name, String type, int math, int english, int science, String result) {
        if (verbose) {
            System.out.println(name + " (" + type + "): " + math + "/" + english + "/" + science + " -> " + result);
        }
    }

    public static String judge(String name, int math, int english, int science, String type) {
        if (!isValidName(name) || !isValidScore(math) || !isValidScore(english) || !isValidScore(science)) {
            return "INVALID";
        }
        String result = switch (type) {
            case "general" -> judgeGeneral(math, english, science);
            case "scholarship" -> judgeScholarship(math, english, science);
            case "transfer" -> judgeTransfer(math, english);
            default -> "INVALID";
        };
        if (!result.equals("INVALID")) {
            log(name, type, math, english, science, result);
        }
        return result;
    }
}
