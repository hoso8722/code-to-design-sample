// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        JudgementService service = new JudgementService();
        ScreenBoard board = new ScreenBoard();
        Stats stats = new Stats();
        service.addListener(board);
        service.addListener(stats);
        service.addListener(new FileLog());
        service.addListener(new AuditLog());
        service.judge("佐藤", 210);
        service.judge("鈴木", 170);
        service.judge("高橋", 190);
        System.out.println(service.summary());
    }
}
