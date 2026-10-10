package example.books;

// 在庫がなくて貸し出せない
public class BookUnavailableException extends RuntimeException {
    public BookUnavailableException(long id) {
        super("貸し出せる在庫がありません: id=" + id);
    }
}
