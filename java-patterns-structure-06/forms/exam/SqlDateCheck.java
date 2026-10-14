// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

// java.util.Date.toInstant() は、java.sql.Date では使えない
public class SqlDateCheck {
    public static void main(String[] args) {
        java.util.Date utilDate = new java.util.Date();
        System.out.println("java.util.Date の toInstant(): " + (utilDate.toInstant() != null ? "使えた" : "null"));
        java.util.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
        try {
            sqlDate.toInstant();
            System.out.println("java.sql.Date の toInstant(): 使えた");
        } catch (UnsupportedOperationException e) {
            System.out.println("java.sql.Date の toInstant(): " + e.getClass().getSimpleName());
        }
    }
}
