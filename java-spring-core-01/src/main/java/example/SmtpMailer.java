package example;

// 本物のメールを送る実装(実験では、送った回数だけを数える)
public class SmtpMailer implements Mailer {
    public static int sent = 0;

    @Override
    public void send(String to, String message) {
        sent++;
        System.out.println("[SMTP] " + to + " へ送信: " + message);
    }
}
