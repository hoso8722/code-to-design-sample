package example.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

// app.loan.* の設定を、まとめて受け取る
@ConfigurationProperties("app.loan")
public record LoanProperties(int days, int maxCount) {}
