// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.Random;

// 不正な値（範囲外の点数など）が混じった入力を1万件作り、不正なオブジェクトが何件できてしまうかを数える
public class Validation {
    record Input(String name, int math, int english, int science, int bonus, int preference) {}

    public static void main(String[] args) {
        Random random = new Random(42);
        int total = 10_000;
        int legacyInvalid = 0, recordInvalid = 0, builderInvalid = 0, rejectedByRecord = 0, rejectedByBuilder = 0;
        int injected = 0;

        for (int i = 0; i < total; i++) {
            boolean bad = random.nextInt(10) == 0;   // 10%に、不正な値を混ぜる
            Input in = new Input("受験者" + i, random.nextInt(101), random.nextInt(101), random.nextInt(101),
                    random.nextInt(21), 1 + random.nextInt(3));
            if (bad) {
                injected++;
                in = switch (random.nextInt(3)) {
                    case 0 -> new Input("", in.math(), in.english(), in.science(), in.bonus(), in.preference());
                    case 1 -> new Input(in.name(), in.math() + 50, in.english(), in.science(), in.bonus(), in.preference());
                    default -> new Input(in.name(), in.math(), in.english(), in.science(), in.bonus(), in.preference() + 9);
                };
            }

            // 検証のない、引数の多いコンストラクタ
            var legacy = new LegacyApplication(in.name(), in.math(), in.english(), in.science(), in.bonus(), in.preference(), false, false);
            if (!legacy.isValid()) legacyInvalid++;

            // record のコンパクトコンストラクタ
            try {
                new Application(in.name(), in.math(), in.english(), in.science(), in.bonus(), in.preference(), false, false);
            } catch (IllegalArgumentException e) {
                rejectedByRecord++;
            }

            // Builder（build() で record に検証を任せる）
            try {
                new ApplicationBuilder().name(in.name()).math(in.math()).english(in.english()).science(in.science())
                        .bonus(in.bonus()).preference(in.preference()).build();
            } catch (IllegalArgumentException e) {
                rejectedByBuilder++;
            }
        }
        System.out.println("入力の件数: " + total + "（不正な値を混ぜた件数: " + injected + "）");
        System.out.println("検証のないコンストラクタで、できてしまった不正なオブジェクト: " + legacyInvalid + " 件");
        System.out.println("record のコンパクトコンストラクタで、拒否した件数: " + rejectedByRecord + " 件");
        System.out.println("Builder の build() で、拒否した件数: " + rejectedByBuilder + " 件");

        // 必須の項目を設定し忘れた場合
        try {
            new ApplicationBuilder().name("佐藤").math(60).english(90).build();
        } catch (IllegalStateException e) {
            System.out.println("科目を設定し忘れた Builder: " + e.getMessage() + "（build() を呼んだ時点で分かる）");
        }
    }
}
