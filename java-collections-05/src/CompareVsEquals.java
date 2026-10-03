// 実行: java CompareVsEquals.java
import java.math.BigDecimal;
import java.util.*;

public class CompareVsEquals {
    public static void main(String[] args) {
        BigDecimal x = new BigDecimal("1.0");
        BigDecimal y = new BigDecimal("1.00");
        System.out.println("x.equals(y)    = " + x.equals(y));       // 桁数（スケール）も比べる
        System.out.println("x.compareTo(y) = " + x.compareTo(y));    // 値だけを比べる

        Set<BigDecimal> hash = new HashSet<>(List.of(x, y));
        Set<BigDecimal> tree = new TreeSet<>(List.of(x, y));
        System.out.println("HashSet: size=" + hash.size() + " " + hash);
        System.out.println("TreeSet: size=" + tree.size() + " " + tree);
    }
}
