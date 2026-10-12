package example;

import java.util.Optional;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final PurgeService purgeService;

    public OrderService(OrderRepository repository, PurgeService purgeService) {
        this.repository = repository;
        this.purgeService = purgeService;
    }

    // 1. よくない例:ログイン済みなら、誰の注文でも返す
    public Optional<Order> findUnchecked(long id) {
        return repository.find(id);
    }

    // 2. 自前のチェック:呼び出した利用者が持ち主かを、業務のコードで確かめる
    public Optional<Order> findOwned(long id, String username) {
        // 他人の注文は、存在しないものとして扱う(存在そのものを知らせない)
        return repository.find(id).filter(order -> order.owner().equals(username));
    }

    // 3. Spring Securityのメソッドセキュリティ:戻り値の持ち主を、メソッドの後で確かめる
    @PostAuthorize("returnObject.isEmpty() or returnObject.get().owner() == authentication.name")
    public Optional<Order> findAuthorized(long id) {
        return repository.find(id);
    }

    // 管理者だけに許す操作
    @PreAuthorize("hasRole('ADMIN')")
    public String purge() {
        return "全注文を消去した(模擬)";
    }

    // 自己呼び出し:誰でも呼べる入口から、管理者だけのメソッドを this で呼ぶ
    public String purgeViaSelf() {
        return purge();
    }

    // 別のBeanに切り出した管理者用のメソッドを、経由して呼ぶ
    public String purgeViaOtherBean() {
        return purgeService.purge();
    }
}
