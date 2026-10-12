// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.TxSelfInvocation
package example;

import javax.sql.DataSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

public class TxSelfInvocation {
    // 挿入してから例外を投げる(トランザクションの中なら、取り消されるはず)
    public static class Registry {
        private final JdbcTemplate jdbc;

        public Registry(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        @Transactional
        public void insertThenFail(String name) {
            jdbc.update("insert into spring_core06_member(name) values (?)", name);
            throw new IllegalStateException("挿入のあとで失敗");
        }

        // 同じクラスの中から、@Transactional のメソッドを呼ぶ
        public void outer(String name) {
            insertThenFail(name);
        }
    }

    // 別のBeanから呼ぶ
    public static class Caller {
        private final Registry registry;

        public Caller(Registry registry) {
            this.registry = registry;
        }

        public void call(String name) {
            registry.insertThenFail(name);
        }
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    static class Config {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(System.getenv("JDBC_URL"), System.getenv("JDBC_USER"), System.getenv("JDBC_PASSWORD"));
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        Registry registry(JdbcTemplate jdbc) {
            return new Registry(jdbc);
        }

        @Bean
        Caller caller(Registry registry) {
            return new Caller(registry);
        }
    }

    static int count(JdbcTemplate jdbc, String name) {
        return jdbc.queryForObject("select count(*) from spring_core06_member where name = ?", Integer.class, name);
    }

    static void attempt(String label, Runnable action, JdbcTemplate jdbc, String name) {
        try {
            action.run();
        } catch (IllegalStateException e) {
            System.out.println("  例外が投げられた: " + e.getMessage());
        }
        System.out.println("  " + label + " のあと、表に残っている行: " + count(jdbc, name));
    }

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            JdbcTemplate jdbc = context.getBean(JdbcTemplate.class);
            jdbc.execute("drop table if exists spring_core06_member");
            jdbc.execute("create table spring_core06_member(name text)");

            Registry registry = context.getBean(Registry.class);
            System.out.println("1. 外から @Transactional のメソッドを直接呼ぶ");
            attempt("insertThenFail", () -> registry.insertThenFail("direct"), jdbc, "direct");

            System.out.println("2. 同じクラスの中から呼ぶ(自己呼び出し)");
            attempt("outer", () -> registry.outer("self"), jdbc, "self");

            System.out.println("3. 別のBeanから呼ぶ");
            attempt("call", () -> context.getBean(Caller.class).call("other"), jdbc, "other");

            jdbc.execute("drop table spring_core06_member");
        }
    }
}
