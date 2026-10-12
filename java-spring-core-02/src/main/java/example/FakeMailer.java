package example;

public class FakeMailer implements Mailer {
    @Override
    public String send(String to, String message) {
        return to + ": " + message;
    }
}
