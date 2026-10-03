// 実行: java Deadlock.java
// 別のターミナルで: jcmd <pid> Thread.print
public class Deadlock {
    static final Object accountA = new Object();
    static final Object accountB = new Object();

    // first のロックを取ってから、second のロックを取る
    static void transfer(String name, Object first, Object second) {
        synchronized (first) {
            System.out.println(name + ": 1つ目のロックを取った");
            sleep(100);  // 相手が1つ目のロックを取るのを待つ（タイミングを合わせるため）
            synchronized (second) {
                System.out.println(name + ": 2つ目のロックも取った");  // ここには来ない
            }
        }
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws Exception {
        // A→B の順と、B→A の順でロックを取る2本のスレッド
        Thread t1 = new Thread(() -> transfer("transfer-1", accountA, accountB), "transfer-1");
        Thread t2 = new Thread(() -> transfer("transfer-2", accountB, accountA), "transfer-2");
        t1.setDaemon(true);  // 止まったままでも、main が終わればプロセスは終わるようにする
        t2.setDaemon(true);
        t1.start();
        t2.start();

        t1.join(2000);
        System.out.println("main: 2秒待った。transfer-1 の状態 = " + t1.getState() + "、transfer-2 の状態 = " + t2.getState());
        System.out.println("main: pid=" + ProcessHandle.current().pid() + "（20秒のうちに jcmd で調べる）");
        Thread.sleep(20_000);
    }
}
