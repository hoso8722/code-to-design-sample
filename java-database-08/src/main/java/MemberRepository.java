// 実行: mvn test
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

public class MemberRepository {
    private static final String UNIQUE_VIOLATION = "23505";

    private final DataSource dataSource;

    public MemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 会員を登録して、割り当てられたIDを返す。メールアドレスが重複したら DuplicateEmailException
    public long register(String email, String displayName) throws SQLException, DuplicateEmailException {
        try (Connection connection = dataSource.getConnection()) {
            return insert(connection, email, displayName);
        }
    }

    private long insert(Connection connection, String email, String displayName)
            throws SQLException, DuplicateEmailException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO members (email, display_name) VALUES (?, ?) RETURNING id")) {
            statement.setString(1, Emails.normalize(email));
            statement.setString(2, displayName);
            try (ResultSet results = statement.executeQuery()) {
                results.next();
                return results.getLong(1);
            }
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw new DuplicateEmailException(email, e);
            }
            throw e;
        }
    }

    public Optional<Member> findByEmail(String email) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, email, display_name FROM members WHERE email = ?")) {
            statement.setString(1, Emails.normalize(email));
            try (ResultSet results = statement.executeQuery()) {
                return results.next()
                        ? Optional.of(new Member(results.getLong(1), results.getString(2), results.getString(3)))
                        : Optional.empty();
            }
        }
    }

    // すでに登録済みなら何もしない(PostgreSQLの ON CONFLICT を使う)。登録したら true
    public boolean registerIfAbsent(String email, String displayName) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO members (email, display_name) VALUES (?, ?) ON CONFLICT (email) DO NOTHING")) {
            statement.setString(1, Emails.normalize(email));
            statement.setString(2, displayName);
            return statement.executeUpdate() == 1;
        }
    }

    // 複数の会員を、1つのトランザクションで登録する。1人でも失敗したら、全員を取り消す
    public void registerAll(List<String[]> emailAndNames) throws SQLException, DuplicateEmailException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (String[] pair : emailAndNames) {
                    insert(connection, pair[0], pair[1]);
                }
                connection.commit();
            } catch (SQLException | DuplicateEmailException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
