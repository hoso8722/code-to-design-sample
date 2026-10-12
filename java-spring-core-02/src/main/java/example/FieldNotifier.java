package example;

import org.springframework.beans.factory.annotation.Autowired;

// フィールド注入:Springなしでは、ふつうの方法で依存を渡せない(privateのフィールドに、リフレクション以外では入れられない)
public class FieldNotifier {
    @Autowired
    private Mailer mailer;

    public String notifyLoan(String user) {
        return mailer.send(user, "貸し出しました");
    }
}
