// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class App {
    public static void main(String[] args) {
        List<Form> forms = List.of(
            new Form("佐藤", "sato@example.com", 60, 70, 80),
            new Form("", "no-at-mark", 101, 70, 80),
            new Form("<鈴木>", "suzuki@example.com", 50, 50, 50));
        for (Form form : forms) {
            System.out.println(form.name() + " -> " + Validation.validate(form));
        }
    }
}
