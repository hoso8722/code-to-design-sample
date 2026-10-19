// 実行: java Main.java
public class Main {
    public static void main(String[] args) {
        Student tanaka = new Student("田中", 75);
        Student suzuki = new Student("鈴木", 55);

        System.out.println(tanaka.describe());
        System.out.println(suzuki.describe());

        tanaka.score = 40;    // フィールドを書き換える
        System.out.println("田中さんの点数を40にした後: " + tanaka.describe());
        System.out.println("鈴木さんは変わらない: " + suzuki.describe());
    }
}

// Student クラスを作り、同じファイルの main から使う
class Student {
    String name;      // フィールド: オブジェクトが持つデータ
    int score;

    // コンストラクタ: new のときに呼ばれ、初期化をする
    Student(String name, int score) {
        this.name = name;     // this.name はフィールド、右辺の name は引数
        this.score = score;
    }

    // インスタンスメソッド: そのオブジェクトのデータを使う
    boolean isPassed() {
        return score >= 60;
    }

    String describe() {
        return name + " さん: " + score + "点(" + (isPassed() ? "合格" : "不合格") + ")";
    }
}
