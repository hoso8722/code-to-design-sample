package example.books;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 業務の例外を、HTTPの状態に変換する(HTTPの知識は、ここと BookController に閉じる)
@RestControllerAdvice
public class DomainErrorHandler {
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Void> onNotFound(BookNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(BookUnavailableException.class)
    public ResponseEntity<Void> onUnavailable(BookUnavailableException e) {
        return ResponseEntity.status(409).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> onBadArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(DuplicateTitleException.class)
    public ResponseEntity<Void> onDuplicate(DuplicateTitleException e) {
        return ResponseEntity.status(409).build();
    }
}
