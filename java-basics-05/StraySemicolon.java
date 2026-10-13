// 実行: java StraySemicolon.java
void main() {
    for (int i = 1; i <= 3; i++);   // 末尾の ; で、繰り返す文が「空」になっている
    {
        IO.println("これは繰り返しの中のつもり");
    }
}
