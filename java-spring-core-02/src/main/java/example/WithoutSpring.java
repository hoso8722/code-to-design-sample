// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.WithoutSpring
package example;

public class WithoutSpring {
    public static void main(String[] args) {
        // Springを使わずに、3つの注入方法のクラスを使ってみる
        System.out.println("コンストラクタ: " + new CtorNotifier(new FakeMailer()).notifyLoan("sora"));

        SetterNotifier setter = new SetterNotifier();
        try {
            setter.notifyLoan("sora");
        } catch (NullPointerException e) {
            System.out.println("setter: setMailer を呼び忘れると NullPointerException");
        }
        setter.setMailer(new FakeMailer());
        System.out.println("setter(呼んだあと): " + setter.notifyLoan("sora"));

        FieldNotifier field = new FieldNotifier();
        try {
            field.notifyLoan("sora");
        } catch (NullPointerException e) {
            System.out.println("フィールド: Springなしでは、ふつうの方法で依存を渡せず、NullPointerException");
        }
    }
}
