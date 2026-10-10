package example.books;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 例外を応答の状態に変換する。予期しない例外は、ここで1回だけ、ERRORとして記録する
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<Void> onNotFound(BookNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({DuplicateTitleException.class, BookUnavailableException.class})
    public ResponseEntity<Void> onConflict(RuntimeException e) {
        return ResponseEntity.status(409).build();
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Void> onUnexpected(Exception e, HttpServletRequest request) {
        log.error("予期しないエラー path={}", request.getRequestURI(), e);
        return ResponseEntity.internalServerError().build();
    }
}
