// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

public class Demo {
    public static void main(String[] args) {
        Node modernTree = new Group("全体", List.of(
            new Group("理系", List.of(new Leaf("数学", 80), new Leaf("理科", 70))),
            new Group("文系", List.of(new Leaf("英語", 60)))));
        System.out.println("modern : " + Node.describe(modernTree) + " 合計=" + Node.total(modernTree));
    }
}
