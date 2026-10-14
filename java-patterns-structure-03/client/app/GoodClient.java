// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package app;

import exam.PassingRule;
import exam.PassingRules;

public class GoodClient {
    public static void main(String[] args) {
        PassingRule rule = PassingRules.of("scholarship");
        System.out.println(rule.describe());
    }
}
