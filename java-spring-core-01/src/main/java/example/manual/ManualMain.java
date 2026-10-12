// 実行: mvn -q compile && java -cp target/classes:<依存のjar> example.manual.ManualMain
package example.manual;

import example.FakeMailer;
import example.Notifier;
import example.SmtpMailer;
import example.bad.BadNotifier;

public class ManualMain {
    public static void main(String[] args) {
        // 1. 依存を自分でnewするクラス: テストのつもりでも、本物の送信が走る
        new BadNotifier().notifyLoan("sora", "こころ");
        System.out.println("本物の送信の回数: " + SmtpMailer.sent);

        // 2. 依存を引数で受け取るクラス: 呼び出す側(ここ)が、組み立てを決める
        FakeMailer fake = new FakeMailer();
        new Notifier(fake).notifyLoan("sora", "こころ");
        System.out.println("テスト用の送信箱: " + fake.outbox);
        System.out.println("本物の送信の回数: " + SmtpMailer.sent);

        // 本番の組み立ても、ここに書く
        new Notifier(new SmtpMailer()).notifyLoan("riku", "坊っちゃん");
    }
}
