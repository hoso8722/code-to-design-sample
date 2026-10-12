package example;

import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
    private final Map<Long, Order> orders = Map.of(
            1L, new Order(1, "alice", "こころ"),
            2L, new Order(2, "bob", "坊っちゃん"));

    public Optional<Order> find(long id) {
        return Optional.ofNullable(orders.get(id));
    }
}
