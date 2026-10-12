package example.resource;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableMethodSecurity
public class ResourceApp {
    // 署名の鍵は認可サーバーの公開鍵(JWK Set)から取得し、iss、aud、期限を検証する
    @Bean
    JwtDecoder jwtDecoder(@Value("${app.issuer}") String issuer, @Value("${app.clock-skew-seconds:0}") long skewSeconds) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(issuer + "/oauth2/jwks").build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ofSeconds(skewSeconds)),
                new JwtIssuerValidator(issuer),
                audience("orders-api")));
        return decoder;
    }

    static OAuth2TokenValidator<Jwt> audience(String expected) {
        return jwt -> jwt.getAudience() != null && jwt.getAudience().contains(expected)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "aud が " + expected + " ではない", null));
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Cookieではなく、Authorization ヘッダーのトークンで認証するAPI
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasAuthority("SCOPE_read")
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasAuthority("SCOPE_write")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @RestController
    static class OrderController {
        @GetMapping("/api/orders")
        List<String> list() {
            return List.of("注文1", "注文2");
        }

        @PostMapping("/api/orders")
        String create() {
            return "注文を作成した";
        }

        // URL単位ではなく、メソッド単位でスコープを求める例
        @PreAuthorize("hasAuthority('SCOPE_write')")
        @PostMapping("/api/orders/cancel")
        String cancel() {
            return "注文を取り消した";
        }

        @GetMapping("/api/me")
        String me(@AuthenticationPrincipal Jwt jwt) {
            return "client=" + jwt.getSubject() + " scope=" + jwt.getClaimAsString("scope");
        }
    }
}
