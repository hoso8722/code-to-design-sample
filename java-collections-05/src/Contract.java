// 実行: java Contract.java
import java.util.*;

public class Contract {
    // 1. equals だけを実装して、hashCode を実装し忘れたクラス
    static final class EqualsOnly {
        final String name;
        EqualsOnly(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof EqualsOnly other && name.equals(other.name);
        }
    }

    // 2. equals と hashCode の両方を実装したクラス
    static final class Both {
        final String name;
        Both(String name) { this.name = name; }
        @Override public boolean equals(Object o) {
            return o instanceof Both other && name.equals(other.name);
        }
        @Override public int hashCode() { return Objects.hash(name); }
    }

    // 3. record（equals と hashCode が自動で作られる）
    record Rec(String name) {}

    public static void main(String[] args) {
        Set<Object> a = new HashSet<>(List.of(new EqualsOnly("alice")));
        a.add(new EqualsOnly("alice"));
        System.out.printf("equals だけ   : size=%d, contains(新しい alice)=%b%n", a.size(), a.contains(new EqualsOnly("alice")));

        Set<Object> b = new HashSet<>(List.of(new Both("alice")));
        b.add(new Both("alice"));
        System.out.printf("両方を実装    : size=%d, contains(新しい alice)=%b%n", b.size(), b.contains(new Both("alice")));

        Set<Object> c = new HashSet<>(List.of(new Rec("alice")));
        c.add(new Rec("alice"));
        System.out.printf("record        : size=%d, contains(新しい alice)=%b%n", c.size(), c.contains(new Rec("alice")));
    }
}
