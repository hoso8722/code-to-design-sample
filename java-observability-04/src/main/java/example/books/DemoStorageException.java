package example.books;

// 保存の失敗を表す例外(実験用)
public class DemoStorageException extends RuntimeException {
    public DemoStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
