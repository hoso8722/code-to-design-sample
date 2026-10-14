package exam;

import java.util.List;

// 親クラス（版1）：各行を、検査してから処理する
public class BaseProcess {
    protected boolean validate(String line) {
        return line.split(",").length == 4;
    }

    public int run(List<String> lines) {
        int processed = 0;
        for (String line : lines) {
            if (validate(line)) {
                processed++;
            }
        }
        return processed;
    }
}
