// 検証しない: コンパイルエラーを確かめる例。collect.sh でコンパイルしている
package exam;

public class Bad {
    public static void main(String[] args) {
        // 申込済みの状態で、いきなり発表しようとする
        new Typed.Applied().announce();
    }
}
