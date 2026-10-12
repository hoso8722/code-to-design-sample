// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.ConditionalMain
package example.cond;

import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.MapPropertySource;

public class ConditionalMain {
    @Configuration
    @Import(RankingFeature.class)
    static class Config {
        // 機能が有効なときだけ使う側は、ObjectProviderで「あれば」受け取る
        @Bean
        Home home(ObjectProvider<RankingFeature> ranking) {
            return new Home(ranking.getIfAvailable());
        }
    }

    static class Home {
        private final RankingFeature ranking;

        Home(RankingFeature ranking) {
            this.ranking = ranking;
        }

        String render() {
            return ranking == null ? "ランキングなしのホーム" : "ホーム + " + ranking.top();
        }
    }

    static void run(String label, Map<String, Object> properties) {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));
            context.register(Config.class);
            context.refresh();
            System.out.println(label);
            System.out.println("  RankingFeatureが登録されたか: " + (context.getBeanNamesForType(RankingFeature.class).length > 0));
            System.out.println("  " + context.getBean(Home.class).render());
        }
    }

    public static void main(String[] args) {
        run("設定なし", Map.of());
        run("app.feature.ranking=true", Map.of("app.feature.ranking", "true"));
        run("app.feature.ranking=false", Map.of("app.feature.ranking", "false"));
    }
}
