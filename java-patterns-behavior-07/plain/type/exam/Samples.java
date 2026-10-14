// 検証しない: 動作確認用の入力
package exam;

import java.util.List;

public class Samples {
    public static final List<Subject> ALL = List.of(
    new Required("数学", 80),
    new Elective("音楽", 90),
    new Practical("実験", 70),
    new Required("英語", 105),
    new Oral("面接", 60));
}
