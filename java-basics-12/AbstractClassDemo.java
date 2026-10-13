// 実行: java AbstractClassDemo.java
abstract class Report {
    // 共通の流れは、親が実装する
    String render() {
        return "【" + title() + "】" + body();
    }

    // 子クラスが必ず実装するもの
    abstract String title();

    abstract String body();
}

class ScoreReport extends Report {
    @Override
    String title() {
        return "成績";
    }

    @Override
    String body() {
        return "田中 75点";
    }
}

public class AbstractClassDemo {
    public static void main(String[] args) {
        Report report = new ScoreReport();
        System.out.println(report.render());
    }
}
