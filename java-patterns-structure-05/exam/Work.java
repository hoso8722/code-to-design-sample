package exam;

// 設定を読み込むふりをする処理。実際のアプリケーションでは、ファイルや環境変数を読む
final class Work {
    private Work() {}

    static int loadPassLine() {
        int x = 0;
        for (int i = 0; i < 20_000; i++) x += i % 7;
        return 180 + (x > 0 ? 0 : 1);
    }
}
