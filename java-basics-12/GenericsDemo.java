// 実行: java GenericsDemo.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class GenericsDemo {
    // ジェネリクスのメソッド
    static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static void main(String[] args) {
        List<String> names = new ArrayList<>();      // String だけを入れるリスト
        names.add("田中");
        names.add("鈴木");
        String firstName = names.get(0);             // キャスト不要で、String として取り出せる
        System.out.println("先頭: " + firstName + "、件数: " + names.size());

        Map<String, Integer> scores = new HashMap<>();
        scores.put("田中", 75);
        scores.put("鈴木", 55);
        System.out.println("田中の点数: " + scores.get("田中"));

        Box<String> stringBox = new Box<>("文字列");
        Box<Integer> intBox = new Box<>(42);
        System.out.println(stringBox.get() + " と " + intBox.get());

        System.out.println("first(names) = " + first(names));
        System.out.println("first(List.of(1, 2, 3)) = " + first(List.of(1, 2, 3)));
    }
}

class Box<T> {                           // T: 使うときに決まる型
    private final T value;

    Box(T value) {
        this.value = value;
    }

    T get() {
        return value;
    }
}
