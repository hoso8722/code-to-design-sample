// 実行: java Recursion.java
long factorial(int n) {
    if (n <= 1) {
        return 1;                        // 終わりの条件
    }
    return n * factorial(n - 1);         // 自分自身を呼ぶ
}

int depth = 0;

void deeper() {
    depth++;
    deeper();                            // 終わりの条件がない
}

void main() {
    IO.println("5! = " + factorial(5));
    IO.println("20! = " + factorial(20));

    try {
        deeper();
    } catch (StackOverflowError e) {
        IO.println("StackOverflowError が起きた。呼び出しの深さは1000以上: " + (depth > 1000));
    }
}
