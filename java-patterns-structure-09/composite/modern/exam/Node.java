package exam;

import java.util.List;

// sealed interface と record：種類を閉じて、不変にする
public sealed interface Node permits Leaf, Group {
    static int total(Node node) {
        return switch (node) {
            case Leaf leaf -> leaf.points();
            case Group group -> {
                int sum = 0;
                for (Node child : group.children()) sum += total(child);
                yield sum;
            }
        };
    }

    static String describe(Node node) {
        return switch (node) {
            case Leaf leaf -> leaf.name() + "=" + leaf.points();
            case Group group -> group.name() + group.children().stream().map(Node::describe).toList();
        };
    }
}
