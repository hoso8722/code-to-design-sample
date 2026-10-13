// 実行: java ThisKeyword.java
class Counter {
    int count;

    Counter(int count) {
        this.count = count;
    }

    Counter increment() {
        this.count++;
        return this;          // this: 呼び出されたオブジェクト自身
    }
}

class Wrong {
    int value;

    Wrong(int value) {
        value = value;        // 引数 value に引数 value を代入している(フィールドには入らない)
    }
}

public class ThisKeyword {
    public static void main(String[] args) {
        Counter c = new Counter(0);
        c.increment().increment().increment();     // return this で、続けて呼べる
        System.out.println("Counter: " + c.count);

        Wrong w = new Wrong(10);
        System.out.println("this を付け忘れたときのフィールドの値: " + w.value);
    }
}
