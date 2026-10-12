package example.authserver;

import java.util.List;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@SpringBootApplication
public class AuthServerApp {
    // アクセストークンの aud(宛先のAPI)を、クライアントごとに決める
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> audience() {
        return context -> {
            String clientId = context.getRegisteredClient().getClientId();
            context.getClaims().audience(List.of(clientId.equals("billing-client") ? "billing-api" : "orders-api"));
        };
    }
}
