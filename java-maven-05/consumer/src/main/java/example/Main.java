// 実行: mvn -q compile exec:java -Dexec.mainClass=example.Main
package example;

public class Main {
    public static void main(String[] args) {
        System.out.println(Greeting.message());
    }
}
