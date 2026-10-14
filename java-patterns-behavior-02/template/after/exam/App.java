// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

public class App {
    public static void main(String[] args) {
        System.out.println(new GeneralProcess().run(Samples.LINES));
        System.out.println(new ScholarshipProcess().run(Samples.LINES));
        System.out.println(new TransferProcess().run(Samples.LINES));
    }
}
