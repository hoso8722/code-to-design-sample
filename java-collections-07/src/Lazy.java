// 実行: java Lazy.java
import java.util.List;
import java.util.stream.Stream;

public class Lazy {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

        System.out.println("== 1. 終端操作のないストリーム");
        Stream<Integer> pipeline = numbers.stream()
                .peek(n -> System.out.println("  filter の前: " + n))
                .filter(n -> n % 2 == 0);
        System.out.println("  （ストリームを作っただけ。まだ何も出力されない）");

        System.out.println("== 2. 終端操作（toList）をつなぐ");
        List<Integer> result = pipeline
                .peek(n -> System.out.println("  map の前   : " + n))
                .map(n -> n * 10)
                .toList();
        System.out.println("  結果: " + result);

        System.out.println("== 3. findFirst（最初の1つが見つかったら終わる）");
        int first = numbers.stream()
                .peek(n -> System.out.println("  filter の前: " + n))
                .filter(n -> n > 2)
                .findFirst()
                .orElseThrow();
        System.out.println("  結果: " + first);
    }
}
