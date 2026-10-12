package example;

import org.springframework.stereotype.Service;

// interface を持たないクラス
@Service
public class ReportService {
    public String report() {
        return "レポート";
    }
}
