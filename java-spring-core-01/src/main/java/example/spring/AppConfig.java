package example.spring;

import example.Mailer;
import example.Notifier;
import example.SmtpMailer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 組み立ての方法を、Springのコンテナに伝える設定クラス
@Configuration
public class AppConfig {
    @Bean
    public Mailer mailer() {
        return new SmtpMailer();
    }

    @Bean
    public Notifier notifier(Mailer mailer) {
        return new Notifier(mailer);
    }
}
