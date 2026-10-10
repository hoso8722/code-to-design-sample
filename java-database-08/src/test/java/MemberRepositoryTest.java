// 実行: mvn test
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Tag("integration") // Dockerが必要なテスト。通常の単体テストとは分けて実行できる
@Testcontainers
class MemberRepositoryTest {
    // 固定のバージョンのPostgreSQLを、テストのたびに起動する。使い捨てのコンテナ
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    static PGSimpleDataSource dataSource;
    MemberRepository repository;

    @BeforeAll
    static void migrate() throws Exception {
        dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(Files.readString(Path.of("src/test/resources/schema.sql")));
        }
    }

    @BeforeEach
    void cleanUp() throws SQLException {
        // テストごとに、表を空にして、IDの採番も最初に戻す
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE members RESTART IDENTITY");
        }
        repository = new MemberRepository(dataSource);
    }

    @Test
    void マイグレーションで会員の表ができている() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement();
             var results = statement.executeQuery(
                     "SELECT count(*) FROM information_schema.columns WHERE table_name = 'members'")) {
            results.next();
            assertEquals(4, results.getInt(1));
        }
    }

    @Test
    void 登録した会員を_正規化したメールアドレスで探せる() throws Exception {
        long id = repository.register("  Sora@Example.com ", "空");

        var member = repository.findByEmail("sora@example.com").orElseThrow();
        assertEquals(id, member.id());
        assertEquals("空", member.displayName());
    }

    @Test
    void 同じメールアドレスの登録は_重複の例外になる() throws Exception {
        repository.register("sora@example.com", "空");

        assertThrows(DuplicateEmailException.class, () -> repository.register("SORA@example.com", "別人"));
    }

    @Test
    void ON_CONFLICTで_登録済みなら何もしない() throws Exception {
        assertTrue(repository.registerIfAbsent("rin@example.com", "凛"));
        assertFalse(repository.registerIfAbsent("rin@example.com", "凛(2回目)"));

        assertEquals("凛", repository.findByEmail("rin@example.com").orElseThrow().displayName());
    }

    @Test
    void まとめて登録の途中で失敗したら_先に登録した会員も残らない() throws Exception {
        List<String[]> members = List.of(
                new String[] {"a@example.com", "A"},
                new String[] {"b@example.com", "B"},
                new String[] {"a@example.com", "Aの重複"});

        assertThrows(DuplicateEmailException.class, () -> repository.registerAll(members));

        assertTrue(repository.findByEmail("a@example.com").isEmpty());
        assertTrue(repository.findByEmail("b@example.com").isEmpty());
    }
}
