// 実行: DB_PASSWORD=demo-secret java SecretHandling.java
public class SecretHandling {
    // 秘密の値を包むクラス:toString で中身を出さない
    record Secret(String value) {
        @Override
        public String toString() {
            return "Secret[****]";
        }
    }

    // 設定を表すクラス(よくない例:recordの標準のtoStringは、全部の項目を出す)
    record PlainConfig(String user, String password) {}

    record SafeConfig(String user, Secret password) {}

    public static void main(String[] args) {
        // 秘密は、ソースコードに書かず、環境変数から読む
        String password = System.getenv("DB_PASSWORD");
        if (password == null) {
            throw new IllegalStateException("環境変数 DB_PASSWORD が設定されていない");
        }
        System.out.println("PlainConfig: " + new PlainConfig("app", password));
        System.out.println("SafeConfig:  " + new SafeConfig("app", new Secret(password)));
    }
}
