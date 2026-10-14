package exam;

import java.util.List;

// 親クラス（版2）：処理する前に、検査を通らない行の件数を先に数えるよう「整理」した。run の結果は版1と同じ
public class BaseProcess {
    protected boolean validate(String line) {
        return line.split(",").length == 4;
    }

    public int run(List<String> lines) {
        long invalid = lines.stream().filter(line -> !validate(line)).count();   // 追加：不正な行の件数
        int processed = 0;
        for (String line : lines) {
            if (validate(line)) {
                processed++;
            }
        }
        return processed;
    }
}
