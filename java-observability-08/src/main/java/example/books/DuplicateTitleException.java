package example.books;

// 同じ題名の本がすでにある
public class DuplicateTitleException extends RuntimeException {
    public DuplicateTitleException(String title) {
        super("題名が登録済みです: " + title);
    }
}
