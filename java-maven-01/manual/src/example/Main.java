// 実行: javac -d out src/example/*.java && java -cp out example.Main
package example;

public class Main {
    public static void main(String[] args) {
        System.out.println(new Greeter().greet("Maven"));
    }
}
