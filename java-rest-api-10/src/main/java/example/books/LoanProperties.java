package example.books;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

// 貸出に関する設定。application.yaml の app.loan 以下を、型のある値として受け取る
@ConfigurationProperties(prefix = "app.loan")
@Validated
public record LoanProperties(
        @NotBlank String libraryName,
        @Min(1) @Max(60) int defaultDays) {}
