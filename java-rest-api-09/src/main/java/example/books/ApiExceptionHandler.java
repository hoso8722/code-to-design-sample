package example.books;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// すべてのエラーを、RFC 9457のProblem Details(application/problem+json)で返す
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String TYPE_BASE = "https://api.example.com/problems/";

    public record FieldProblem(String field, String message) {}

    private static ProblemDetail problem(HttpStatus status, String slug, String title, String detail, String instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(TYPE_BASE + slug));
        pd.setTitle(title);
        pd.setInstance(URI.create(instance));
        return pd;
    }

    // --- 業務の例外 ---

    @ExceptionHandler(BookNotFoundException.class)
    public ProblemDetail onNotFound(BookNotFoundException e, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "book-not-found", "本が見つかりません", e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DuplicateTitleException.class)
    public ProblemDetail onDuplicate(DuplicateTitleException e, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "duplicate-title", "題名が重複しています", e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(BookUnavailableException.class)
    public ProblemDetail onUnavailable(BookUnavailableException e, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "book-unavailable", "貸し出せる在庫がありません", e.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail onBadArgument(IllegalArgumentException e, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-argument", "引数が正しくありません", e.getMessage(), request.getRequestURI());
    }

    // --- 検証の失敗(項目ごとのエラーを errors に入れる) ---

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldProblem> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new FieldProblem(f.getField(), f.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldProblem::field))
                .toList();
        return validationResponse(errors, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldProblem> errors = ex.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldProblem(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return validationResponse(errors, request);
    }

    // JSONの構文が壊れている、項目の型が合わない場合(検証の前に失敗するもの)
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "malformed-request", "リクエストの本文を読み取れません",
                "本文がJSONとして正しくないか、項目の型が合っていません。", pathOf(request));
        return ResponseEntity.badRequest().body(pd);
    }

    // 受け持つ処理がないURI
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail pd = problem(HttpStatus.NOT_FOUND, "uri-not-found", "URIが見つかりません",
                "指定されたURIは存在しません。", pathOf(request));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    private ResponseEntity<Object> validationResponse(List<FieldProblem> errors, WebRequest request) {
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "validation-error", "入力に誤りがあります",
                "入力に誤りがあります。errors を確認してください。", pathOf(request));
        pd.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(pd);
    }

    // --- 予期しない失敗:内部の情報は応答に含めず、ログと突き合わせられるIDだけを返す ---

    @ExceptionHandler(Exception.class)
    public ProblemDetail onUnexpected(Exception e, HttpServletRequest request) {
        String errorId = UUID.randomUUID().toString();
        log.error("予期しないエラー errorId={} path={}", errorId, request.getRequestURI(), e);
        ProblemDetail pd = problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error", "サーバー内部でエラーが発生しました",
                "予期しないエラーが発生しました。問い合わせるときは errorId を伝えてください。", request.getRequestURI());
        pd.setProperty("errorId", errorId);
        return pd;
    }

    private static String pathOf(WebRequest request) {
        String description = request.getDescription(false); // "uri=/api/books" の形
        return description.startsWith("uri=") ? description.substring(4) : description;
    }
}
