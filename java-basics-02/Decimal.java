// 実行: java Decimal.java
import java.math.BigDecimal;

void main() {
    IO.println("0.1 + 0.2 = " + (0.1 + 0.2));
    IO.println("0.1 + 0.2 == 0.3 : " + (0.1 + 0.2 == 0.3));
    IO.println("3 * 0.1 = " + 3 * 0.1);
    IO.println("差の絶対値が十分小さいか: " + (Math.abs((0.1 + 0.2) - 0.3) < 1e-9));

    BigDecimal sum = new BigDecimal("0.1").add(new BigDecimal("0.2"));
    IO.println("BigDecimal で足した結果 = " + sum);
    IO.println("BigDecimal の比較 : " + sum.equals(new BigDecimal("0.3")));
}
