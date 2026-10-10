package example.books;

// 貸出の記録の作成に失敗した(検査例外:Exceptionを継承している)
public class LoanRecordException extends Exception {
    public LoanRecordException(String message) {
        super(message);
    }
}
