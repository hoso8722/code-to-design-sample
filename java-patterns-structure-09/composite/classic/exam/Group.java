package exam;

import java.util.ArrayList;
import java.util.List;

public class Group implements Node {
    private final String name;
    private final List<Node> children = new ArrayList<>();

    public Group(String name) { this.name = name; }

    public Group add(Node child) {
        children.add(child);
        return this;
    }

    @Override
    public int total() {
        int sum = 0;
        for (Node child : children) sum += child.total();
        return sum;
    }

    @Override
    public String describe() {
        StringBuilder out = new StringBuilder(name).append("[");
        for (int i = 0; i < children.size(); i++) {
            if (i > 0) out.append(", ");
            out.append(children.get(i).describe());
        }
        return out.append("]").toString();
    }
}
