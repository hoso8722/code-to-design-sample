package example.books;

// 指定された本が存在しない
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super("本が見つかりません: id=" + id);
    }
}
