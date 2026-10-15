// 検証しない: コンパイルエラーを確かめる例。collect.sh でコンパイルしている
package exam;

import java.util.Map;

public class BadAccess {
    public static void main(String[] args) {
        Candidate candidate = new ObjectAdapter(new ExternalApplicant("佐藤", Map.of(), new java.util.Date()));
        // Candidate 型の変数から、外部のクラスのメソッドを呼ぼうとする
        System.out.println(candidate.getFullName());
    }
}
