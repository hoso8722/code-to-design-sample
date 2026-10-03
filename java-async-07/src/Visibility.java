// 実行: java Visibility.java volatile
public class Visibility {
    static boolean stop = false;           // volatile なし
    static volatile boolean stopVolatile = false;

    public static void main(String[] args) throws Exception {
        boolean useVolatile = args.length > 0 && args[0].equals("volatile");
        Thread worker = new Thread(() -> {
            long count = 0;
            if (useVolatile) {
                while (!stopVolatile) count++;
            } else {
                while (!stop) count++;
            }
            System.out.println("worker: 止まった（" + count + " 回まわった）");
        });
        worker.setDaemon(true);  // 止まらなかった場合でも、プロセスは終われるようにする
        worker.start();

        Thread.sleep(500);  // worker のループが JIT コンパイルされるまで待つ
        if (useVolatile) stopVolatile = true; else stop = true;
        System.out.println("main: 止まれと伝えた（volatile " + (useVolatile ? "あり" : "なし") + "）");

        worker.join(2000);  // 最大2秒待つ
        System.out.println("main: 2秒待った。worker は " + (worker.isAlive() ? "まだ動いている" : "止まっている"));
    }
}
