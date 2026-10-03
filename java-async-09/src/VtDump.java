// 実行: java VtDump.java
// 別のターミナルで: jcmd <pid> Thread.print と jcmd <pid> Thread.dump_to_file <ファイル名>
public class VtDump {
    public static void main(String[] a) throws Exception {
        for (int i = 0; i < 3; i++) Thread.ofVirtual().name("vt-" + i).start(() -> { try { Thread.sleep(60_000); } catch (InterruptedException e) {} });
        System.out.println("pid=" + ProcessHandle.current().pid());
        Thread.sleep(30_000);
    }
}
