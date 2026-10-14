// 検証しない: 複数ファイルの例。collect.sh でコンパイルして実行している
package exam;

import java.util.List;

// 理系グループ（数学、理科）と、文系グループ（英語）を含む、成績の木
public class Demo {
    public static void main(String[] args) {
        Node classicTree = new Group("全体")
            .add(new Group("理系").add(new Leaf("数学", 80)).add(new Leaf("理科", 70)))
            .add(new Group("文系").add(new Leaf("英語", 60)));
        System.out.println("classic: " + classicTree.describe() + " 合計=" + classicTree.total());
    }
}
