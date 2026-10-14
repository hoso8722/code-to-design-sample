// 検証しない: 別のパッケージから呼ぶ例。collect.sh でコンパイルエラーを確かめている
package app;

import exam.PassingRule;

public class BadClient {
    public static void main(String[] args) {
        // Factory を経由せず、具体的なクラスを直接作ろうとする
        PassingRule rule = new exam.ScholarshipRule();
        System.out.println(rule.describe());
    }
}
