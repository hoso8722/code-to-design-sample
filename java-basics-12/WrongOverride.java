// 検証しない: コンパイルエラーを見せるための、わざと間違えたコード
class Person {
    String introduce() {
        return "私です";
    }
}

class Student extends Person {
    @Override
    String introdue() {       // 綴りの間違い
        return "学生です";
    }
}

public class WrongOverride {
    public static void main(String[] args) {
        System.out.println(new Student().introduce());
    }
}
