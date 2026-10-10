// 実行: mvn test
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Test;

// H2をPostgreSQLの互換モードで動かしても、PostgreSQL向けのSQLがそのまま通るとは限らないことを確かめる
class H2CompatibilityTest {
    private static final String URL = "jdbc:h2:mem:compat;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";

    private void assertFailsOnH2(String sql) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, "sa", ""); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS probe (id INT PRIMARY KEY, email TEXT UNIQUE)");
            assertThrows(SQLException.class, () -> statement.execute(sql));
        }
    }

    @Test
    void ON_CONFLICTは_H2のPostgreSQLモードでは通らない() throws SQLException {
        assertFailsOnH2("INSERT INTO probe VALUES (1, 'a@example.com') ON CONFLICT (email) DO NOTHING");
    }

    @Test
    void RETURNINGは_H2のPostgreSQLモードでは通らない() throws SQLException {
        assertFailsOnH2("INSERT INTO probe VALUES (2, 'b@example.com') RETURNING id");
    }

    @Test
    void jsonb型は_H2のPostgreSQLモードでは通らない() throws SQLException {
        assertFailsOnH2("SELECT '{\"a\": 1}'::jsonb ->> 'a'");
    }
}
