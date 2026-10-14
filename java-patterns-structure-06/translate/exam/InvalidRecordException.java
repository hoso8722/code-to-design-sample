package exam;

// 外部データの形が不正なことを表す、内側の例外
public class InvalidRecordException extends RuntimeException {
    public InvalidRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}
