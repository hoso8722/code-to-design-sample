// 実行: mvn -q test-compile のあと、java -cp target/classes:target/test-classes exam.Trial
package exam;

import java.util.LinkedHashMap;
import java.util.Map;

// 「整理したつもり」の変更を、いつもの目視（3件）と、特性テスト（全件）で見つけられるかを数える
public class Trial {
    interface Judge { String apply(String name, int m, int e, int s, String type); }

    static int failures(Judge judge, java.util.List<Cases.Case> cases) {
        int count = 0;
        for (Cases.Case c : cases) {
            String actual;
            try {
                actual = judge.apply(c.name(), c.math(), c.english(), c.science(), c.type());
            } catch (RuntimeException ex) {
                actual = ex.getClass().getSimpleName();
            }
            if (!actual.equals(c.expected())) count++;
        }
        return count;
    }

    public static void main(String[] args) {
        Map<String, Judge> variants = new LinkedHashMap<>();
        variants.put("合計の計算を取り出しただけ（正しい整理）", Variants::extracted);
        variants.put("平均を四捨五入にした", Variants::rounded);
        variants.put("合格点の比較を > にした", Variants::greaterThan);
        variants.put("編入にも3科目の合計を使った", Variants::transferAllSubjects);
        variants.put("名前の検査を isBlank() にした", Variants::blankCheck);
        variants.put("奨学の理科の条件が抜けた", Variants::missingScience);

        System.out.printf("%-34s %8s %10s%n", "変更", "目視3件", "特性テスト");
        for (var entry : variants.entrySet()) {
            int manual = failures(entry.getValue(), Cases.MANUAL);
            int all = failures(entry.getValue(), Cases.ALL);
            System.out.printf("%-34s %5d件 %8d件%n", entry.getKey(), manual, all);
        }

        System.out.println();
        System.out.println("1回の大きな書き換え（3つの間違いを同時に入れた）で失敗した特性テスト:");
        for (Cases.Case c : Cases.ALL) {
            String actual = Variants.bigRewrite(c.name(), c.math(), c.english(), c.science(), c.type());
            if (!actual.equals(c.expected())) System.out.println("  " + c.label() + "（期待 " + c.expected() + "、実際 " + actual + "）");
        }
    }
}
