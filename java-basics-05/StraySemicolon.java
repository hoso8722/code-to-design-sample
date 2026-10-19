// 実行: java StraySemicolon.java
public class StraySemicolon {
    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++);   // 末尾の ; で、繰り返す文が「空」になっている
        {
            System.out.println("これは繰り返しの中のつもり");
        }
    }
}
