package example.books;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// 起動したときに、SLF4Jの裏側で動いているloggingの実装を記録する
@Component
public class BackendReporter implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(BackendReporter.class);

    @Override
    public void run(ApplicationArguments args) {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        log.info("SLF4Jの実装(backend): {}", factory.getClass().getName());
    }
}
