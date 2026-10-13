// 実行: java ListModificationQuirk.java
import java.util.ArrayList;
import java.util.List;

public class ListModificationQuirk {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("田中", "鈴木", "佐藤"));
        for (String name : names) {
            if (name.equals("鈴木")) {
                names.remove(name);      // 後ろから2番目の要素を消すと、例外にならない場合がある
            }
        }
        System.out.println(names);
    }
}
