package example.books;

import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

// 検証に失敗したときの応答を、項目ごとのエラーの一覧にそろえる
@RestControllerAdvice
public class ValidationErrorHandler {
    public record FieldProblem(String field, String message) {}

    public record ValidationError(String message, List<FieldProblem> errors) {}

    // 本文(@Valid @RequestBody)の検証に失敗した場合
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> onBody(MethodArgumentNotValidException e) {
        List<FieldProblem> problems = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new FieldProblem(f.getField(), f.getDefaultMessage()))
                .sorted(Comparator.comparing(FieldProblem::field))
                .toList();
        return ResponseEntity.badRequest().body(new ValidationError("入力に誤りがあります", problems));
    }

    // URIの一部や、クエリ文字列の引数に付けた制約に違反した場合
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ValidationError> onParameter(HandlerMethodValidationException e) {
        List<FieldProblem> problems = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new FieldProblem(result.getMethodParameter().getParameterName(), error.getDefaultMessage())))
                .toList();
        return ResponseEntity.badRequest().body(new ValidationError("入力に誤りがあります", problems));
    }
}
