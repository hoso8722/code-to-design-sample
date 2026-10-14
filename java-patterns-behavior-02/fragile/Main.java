// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<String> lines = List.of("佐藤,60,70,80", "鈴木,90,50,40", "高橋,70,70,50", "不正な行");
        CountingProcess process = new CountingProcess();
        int processed = process.run(lines);
        System.out.println("処理した行: " + processed + "、検査した行（子クラスの数え方）: " + process.validated);
    }
}
