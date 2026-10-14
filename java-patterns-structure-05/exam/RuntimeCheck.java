// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// JDK の例：Runtime.getRuntime() は、何度呼んでも同じインスタンスを返す
public class RuntimeCheck {
    public static void main(String[] args) {
        System.out.println("Runtime.getRuntime() == Runtime.getRuntime(): " + (Runtime.getRuntime() == Runtime.getRuntime()));
    }
}
