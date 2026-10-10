package example.books;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class SecurityConfig {
    private static final String TYPE_BASE = "https://api.example.com/problems/";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JsonMapper mapper) throws Exception {
        // 認証が必要なときの応答。Basic認証を求めるヘッダーと、Problem Detailsの本文を返す
        AuthenticationEntryPoint unauthorized = (request, response, e) -> {
            response.setHeader("WWW-Authenticate", "Basic realm=\"books\", charset=\"UTF-8\"");
            writeProblem(mapper, request, response, HttpStatus.UNAUTHORIZED, "unauthorized",
                    "認証が必要です", "認証情報が正しくないか、付いていません。");
        };
        http
                // 認証は、要求ごとの Authorization ヘッダーで行い、セッション(Cookie)は使わない
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.authenticationEntryPoint(unauthorized))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/public/**", "/actuator/health/**").permitAll()          // 公開
                        .requestMatchers(HttpMethod.GET, "/api/books/**").hasAnyRole("READER", "ADMIN") // 読み取り
                        .requestMatchers("/api/books/**").hasRole("ADMIN")                              // 登録、更新、削除、貸出
                        .anyRequest().denyAll())                                                        // 上のどれにも当てはまらないURIは拒否
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((request, response, e) -> writeProblem(mapper, request, response,
                                HttpStatus.FORBIDDEN, "forbidden", "この操作は許可されていません", "必要な権限がありません。")));
        return http.build();
    }

    // パスワードは、ハッシュ化して保存する(bcrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 利用者は2人だけ。パスワードは環境変数から読み、ハッシュ化してメモリに持つ
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder,
                                                 @Value("${app.security.reader-password}") String readerPassword,
                                                 @Value("${app.security.admin-password}") String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("reader").password(encoder.encode(readerPassword)).roles("READER").build(),
                User.withUsername("admin").password(encoder.encode(adminPassword)).roles("ADMIN").build());
    }

    // 認証や認可の失敗も、Problem Details(application/problem+json)で返す
    private static void writeProblem(JsonMapper mapper, HttpServletRequest request, HttpServletResponse response,
                                     HttpStatus status, String slug, String title, String detail) throws IOException {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(TYPE_BASE + slug));
        pd.setTitle(title);
        pd.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(), pd);
    }
}
