// 実行: java Validation.java
import java.util.List;
import java.util.regex.Pattern;

public class Validation {
    // よくない例:危険な文字列を取り除く(拒否リスト)
    static String blacklist(String name) {
        return name.replace("../", "");
    }

    // 改善した例:許可する形を決めて、それ以外を拒否する(許可リスト)
    private static final Pattern ALLOWED = Pattern.compile("[A-Za-z0-9_-]{1,32}\\.(txt|png)");

    static String allowlist(String name) {
        if (!ALLOWED.matcher(name).matches()) {
            throw new IllegalArgumentException("許可されない名前: " + name);
        }
        return name;
    }

    public static void main(String[] args) {
        List<String> inputs = List.of("notice.txt", "../secret.txt", "....//secret.txt", "a/../../secret.txt", "report.png");

        System.out.println("## 拒否リスト(\"../\" を取り除く)");
        for (String input : inputs) {
            System.out.println("  " + input + " → " + blacklist(input));
        }

        System.out.println("## 許可リスト(英数字とハイフンとアンダースコア、拡張子は txt か png)");
        for (String input : inputs) {
            try {
                System.out.println("  " + input + " → " + allowlist(input));
            } catch (IllegalArgumentException e) {
                System.out.println("  " + input + " → 拒否");
            }
        }
    }
}
