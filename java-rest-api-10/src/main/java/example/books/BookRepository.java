package example.books;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

// SQLを実行する層。値は、名前つきのパラメーターで渡す
@Repository
public class BookRepository {
    private final JdbcClient jdbc;

    public BookRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // orderBy は列名。SQLの構造の一部なので、パラメーターにはできない。呼び出し側が許可した値だけを渡す
    public List<BookEntity> findAll(String orderBy) {
        return jdbc.sql("SELECT id, title, copies FROM books ORDER BY " + orderBy)
                .query(BookEntity.class)
                .list();
    }

    public Optional<BookEntity> find(long id) {
        return jdbc.sql("SELECT id, title, copies FROM books WHERE id = :id")
                .param("id", id)
                .query(BookEntity.class)
                .optional();
    }

    public Optional<BookEntity> findByTitle(String title) {
        return jdbc.sql("SELECT id, title, copies FROM books WHERE title = :title")
                .param("title", title)
                .query(BookEntity.class)
                .optional();
    }

    public BookEntity insert(String title, int copies) {
        long id = jdbc.sql("INSERT INTO books (title, copies) VALUES (:title, :copies) RETURNING id")
                .param("title", title)
                .param("copies", copies)
                .query(Long.class)
                .single();
        return new BookEntity(id, title, copies);
    }

    public boolean update(long id, String title, int copies) {
        return jdbc.sql("UPDATE books SET title = :title, copies = :copies WHERE id = :id")
                .param("title", title)
                .param("copies", copies)
                .param("id", id)
                .update() == 1;
    }

    public boolean delete(long id) {
        return jdbc.sql("DELETE FROM books WHERE id = :id").param("id", id).update() == 1;
    }

    // 在庫が1冊以上あるときだけ、1冊減らす。更新できた行数(0か1)を返す
    public int decrementCopies(long id) {
        return jdbc.sql("UPDATE books SET copies = copies - 1 WHERE id = :id AND copies > 0")
                .param("id", id)
                .update();
    }

    public void insertLoan(long bookId, String borrower) {
        jdbc.sql("INSERT INTO loans (book_id, borrower) VALUES (:bookId, :borrower)")
                .param("bookId", bookId)
                .param("borrower", borrower)
                .update();
    }
}
