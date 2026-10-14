// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class BadDirect {
    public static void main(String[] args) {
        List<String> lines = List.of("佐藤,60,70,80", "鈴木,九十,50,40");
        try {
            new ExamService().countPassed(lines);
        } catch (NumberFormatException e) {
            System.out.println("直接読む: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  発生した場所: " + e.getStackTrace()[e.getStackTrace().length - 2]);
        }
    }
}
