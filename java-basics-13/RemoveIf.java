// 実行: java RemoveIf.java
import java.util.ArrayList;
import java.util.List;

public class RemoveIf {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("田中", "鈴木", "佐藤"));
        names.removeIf(name -> name.equals("田中"));     // 走査しながら安全に消す書き方
        System.out.println(names);
    }
}
