package example;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

// 管理者だけの操作を、別のBeanに切り出す
@Service
public class PurgeService {
    @PreAuthorize("hasRole('ADMIN')")
    public String purge() {
        return "全注文を消去した(模擬)";
    }
}
