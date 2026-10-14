package exam;

// 科目の点数（Leaf）と、科目のグループ（Group）を、同じように扱う
public interface Node {
    int total();
    String describe();
}
