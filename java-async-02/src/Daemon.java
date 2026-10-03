// 実行: java Daemon.java daemon
public class Daemon {
    public static void main(String[] args) {
        boolean daemon = args.length > 0 && args[0].equals("daemon");
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("worker: 1秒たったので終わる");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "worker");
        t.setDaemon(daemon);  // start() の前に設定する
        t.start();
        System.out.println("main: 終わる（worker はデーモン? " + t.isDaemon() + "）");
    }
}
