// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class Forms {
    public static void main(String[] args) {
        // 1995年4月2日（日本時間の0時）
        Date birth = Date.from(java.time.LocalDate.of(1995, 4, 2).atStartOfDay(java.time.ZoneId.of("Asia/Tokyo")).toInstant());
        Map<String, Integer> scores = Map.of("math", 60, "english", 70, "science", 80);

        Candidate byInheritance = new ClassAdapter("佐藤", scores, birth);
        Candidate byDelegation = new ObjectAdapter(new ExternalApplicant("佐藤", scores, birth));
        System.out.println("継承版: " + byInheritance.name() + " 合計=" + byInheritance.total() + " 生年月日=" + byInheritance.birthDate());
        System.out.println("委譲版: " + byDelegation.name() + " 合計=" + byDelegation.total() + " 生年月日=" + byDelegation.birthDate());

        // 継承版は、外部のクラスのメソッドも見える（意図しない呼び出しができる）
        System.out.println("継承版から見えるメソッド数: " + ClassAdapter.class.getMethods().length
                + "、委譲版から見えるメソッド数: " + ObjectAdapter.class.getMethods().length);

        ExamFacade facade = new ExamFacade();
        System.out.println(facade.report(List.of(byInheritance,
            new ObjectAdapter(new ExternalApplicant("鈴木", Map.of("math", 90, "english", 50, "science", 40), birth)))));
    }
}
