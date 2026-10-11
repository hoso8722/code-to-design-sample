// 実行: mvn compile （runtimeスコープのクラスを使っているため、コンパイルは失敗する）
package example;

// runtime スコープの依存のクラスを、コンパイル時に使おうとする(コンパイルは通らない)
public class UsePostgres {
    public static void main(String[] args) {
        System.out.println(new org.postgresql.Driver());
    }
}
