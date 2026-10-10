// Pool.java: HikariCPのDataSourceを作る補助クラス
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

final class Pool {
    private Pool() {}

    static HikariDataSource create(int maximumPoolSize, long connectionTimeoutMillis) {
        // HikariCPのログは、警告以上だけを標準エラーに出す
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(Db.required("JDBC_URL"));
        config.setUsername(Db.required("JDBC_USER"));
        config.setPassword(Db.required("JDBC_PASSWORD"));
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(maximumPoolSize); // 固定の大きさにする(同じ大きさで、常に接続を持つ)
        config.setConnectionTimeout(connectionTimeoutMillis);
        return new HikariDataSource(config);
    }
}
