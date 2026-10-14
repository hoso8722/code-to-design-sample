// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class Bad {
    public static void main(String[] args) {
        List<String> lines = List.of("佐藤,60,70,80", "鈴木,九十,50,40");
        try {
            new ExamService(new CsvV1Adapter()).countPassed(lines);
        } catch (InvalidRecordException e) {
            System.out.println("アダプタ経由: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("  原因: " + e.getCause());
        }
    }
}
