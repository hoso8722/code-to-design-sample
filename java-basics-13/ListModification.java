// 検証しない: 実行時エラーを見せるための、わざと間違えたコード
import java.util.ArrayList;
import java.util.List;

public class ListModification {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("田中", "鈴木", "佐藤"));
        for (String name : names) {
            if (name.equals("田中")) {
                names.remove(name);      // 拡張 for の途中で、リストを変更している
            }
        }
        System.out.println(names);
    }
}
