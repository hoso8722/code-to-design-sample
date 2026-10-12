package example.cond;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// app.feature.ranking=true のときだけ、Beanとして登録される
@Component
@ConditionalOnProperty(name = "app.feature.ranking", havingValue = "true")
public class RankingFeature {
    public String top() {
        return "1位: こころ";
    }
}
