package example.books;

// 本の登録に失敗した。何をしようとして失敗したかの文脈を持ち、元の例外を原因として保持する
public class BookRegistrationException extends RuntimeException {
    public BookRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
