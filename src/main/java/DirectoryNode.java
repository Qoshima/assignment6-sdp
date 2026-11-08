import java.util.ArrayList;
import java.util.List;

public class DirectoryNode extends AbstractNode {
    private final List<Element> children;
    public DirectoryNode(String name) {
        super(name);
        this.children = new ArrayList<>();
    }
    public List<Element> getChildren() {
        return List.copyOf(children);
    }
    public void add(AbstractNode child) {
        if (child == null){
            throw new IllegalArgumentException("Cannot add null child");
        }
        for (Element existing : children) {
            if (existing.getName().equals(child.getName())) {
                throw new IllegalArgumentException("Duplicate child name");
            }
        }
        if (child.getParent() != null) {
            throw new IllegalStateException("Child already has a parent");
        }

        DirectoryNode current = this;

        while (current != null) {

            if (current == child) {
                throw new IllegalArgumentException("Cycle detected");
            }

            current = current.getParent();
        }
        child.setParent(this);
        children.add(child);
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
