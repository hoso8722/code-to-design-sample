// 検証しない: パッケージつきの複数ファイルの例のため。実行結果は output-package.txt を参照
package school.util;

public class Stats {
    public static double average(int[] scores) {
        int total = 0;
        for (int score : scores) {
            total += score;
        }
        return (double) total / scores.length;
    }
}
