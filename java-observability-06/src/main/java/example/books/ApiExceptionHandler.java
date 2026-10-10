package example.books;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 例外を応答の状態に変換する。予期しない例外は、ここ(境界)で1回だけ、ERRORとして記録する
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

    // 利用者に返す内容と、診断のために記録する内容を分ける。2つは errorId で結ぶ
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> onUnexpected(Exception e, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("予期しないエラー errorId={} path={}", errorId, request.getRequestURI(), e);
        return ResponseEntity.internalServerError()
                .body(Map.of("message", "予期しないエラーが発生しました", "errorId", errorId));
    }
}
