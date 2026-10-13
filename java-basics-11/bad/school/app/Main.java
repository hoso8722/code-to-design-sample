// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
package school.app;

import school.model.Student;

public class Main {
    public static void main(String[] args) {
        Student tanaka = new Student("田中", 75);
        System.out.println(tanaka.secretNote());
    }
}
