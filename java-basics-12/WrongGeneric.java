// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
import java.util.ArrayList;
import java.util.List;

public class WrongGeneric {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        names.add("田中");
        names.add(123);
        int n = names.get(0);
    }
}
