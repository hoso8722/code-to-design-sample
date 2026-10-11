// 実行: mvn -q dependency:build-classpath -DincludeScope=runtime で得たクラスパスを、java -cp に渡して実行する
public class LoadDriver {
    public static void main(String[] args) throws Exception {
        Class<?> driver = Class.forName("org.postgresql.Driver");
        System.out.println("実行時に読み込めた: " + driver.getName());
    }
}
