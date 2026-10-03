// 実行: java CopyOfSame.java
import java.util.*;

public class CopyOfSame {
    public static void main(String[] args) {
        List<String> immutable = List.of("a", "b", "c");
        List<String> view = Collections.unmodifiableList(new ArrayList<>(immutable));
        System.out.println("List.copyOf(List.of(...)) は同じオブジェクト?       " + (List.copyOf(immutable) == immutable));
        System.out.println("List.copyOf(unmodifiableList(...)) は同じオブジェクト? " + (List.copyOf(view) == view));
        System.out.println("List.of() は毎回同じオブジェクト?                     " + (List.of() == List.of()));
    }
}
