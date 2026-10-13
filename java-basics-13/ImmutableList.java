// 検証しない: 実行時エラーを見せるための、わざと間違えたコード
import java.util.List;

public class ImmutableList {
    public static void main(String[] args) {
        List<String> names = List.of("田中", "鈴木");
        names.add("佐藤");
    }
}
