// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

// 3つのテストを、指定した順序で、1つのJVMの中で実行する（JUnit のように、テスト間でstaticな状態は共有される）
public class Harness {
    public static void main(String[] args) {
        boolean singleton = args[0].equals("singleton");
        List<String> results = new ArrayList<>();
        for (String name : args[1].split(",")) {
            BooleanSupplier test = switch (name) {
                case "T1" -> () -> singleton
                        ? new JudgeWithSingleton().passes(180)                    // 180点ちょうどは合格
                        : new JudgeWithConfig(new Config(180)).passes(180);
                case "T2" -> () -> {
                    if (singleton) {
                        MutableConfig.getInstance().setPassLine(200);             // 合格点を200にする（元に戻さない）
                        return !new JudgeWithSingleton().passes(180);             // 180点は不合格になる
                    }
                    Config config = new Config(200);
                    return !new JudgeWithConfig(config).passes(180);
                };
                case "T3" -> () -> singleton
                        ? MutableConfig.getInstance().passLine() == 180           // 既定の合格点は180
                        : new Config(180).passLine() == 180;
                default -> throw new IllegalArgumentException(name);
            };
            results.add(name + ":" + (test.getAsBoolean() ? "OK" : "NG"));
        }
        System.out.println(String.join(" ", results));
    }
}
