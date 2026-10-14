package exam;

import java.util.List;

public record Group(String name, List<Node> children) implements Node {
    public Group {
        children = List.copyOf(children);   // 不変にする
    }
}
