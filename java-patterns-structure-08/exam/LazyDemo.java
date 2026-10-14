// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// 遅延初期化のプロキシ：本物の生成が、最初の使用まで遅れる
public class LazyDemo {
    public static void main(String[] args) throws Exception {
        Clock.log("アプリケーションを起動した");

        System.out.println("-- 本物を直接作る場合");
        StudentRepository direct = new DatabaseRepository();
        Clock.log("直接作った本物が使える状態になった");

        System.out.println("-- プロキシを作る場合");
        StudentRepository lazy = new LazyRepository(DatabaseRepository::new);
        Clock.log("プロキシが使える状態になった（まだ接続していない）");
        Thread.sleep(500);
        Clock.log("500ミリ秒待った（誰も使わなかった）");
        Clock.log("find を呼ぶ: " + lazy.find("s1"));
        Clock.log("もう一度 find を呼ぶ: " + lazy.find("s2"));
    }
}
