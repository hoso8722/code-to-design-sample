package example;

import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/v1/orders/{id}")
    public ResponseEntity<Order> unchecked(@PathVariable long id) {
        return ResponseEntity.of(service.findUnchecked(id));
    }

    @GetMapping("/v2/orders/{id}")
    public ResponseEntity<Order> owned(@PathVariable long id, Principal principal) {
        return ResponseEntity.of(service.findOwned(id, principal.getName()));
    }

    @GetMapping("/v3/orders/{id}")
    public ResponseEntity<Order> authorized(@PathVariable long id) {
        return ResponseEntity.of(service.findAuthorized(id));
    }

    @PostMapping("/direct/purge")
    public String purgeDirect() {
        return service.purge();
    }

    @PostMapping("/self/purge")
    public String purgeSelf() {
        return service.purgeViaSelf();
    }

    @PostMapping("/other/purge")
    public String purgeOther() {
        return service.purgeViaOtherBean();
    }
}
