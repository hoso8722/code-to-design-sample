package example.domain;

import example.common.Money;
import java.util.List;

public record Order(List<Money> items) {
    public Money total() {
        return items.stream().reduce(new Money(0), Money::plus);
    }
}
