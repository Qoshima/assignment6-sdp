abstract class AbstractNode implements Element {
    private final String name;
    private DirectoryNode parent;
    DirectoryNode getParent() {
        return parent;
    }
    void setParent(DirectoryNode newparent) {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot set parent twice");
        }
        if (newparent == null) {
            throw new IllegalArgumentException("Parent cannot be null");
        }
        this.parent = newparent;
    }
    public AbstractNode(String name) {
        if (name == null || name.contains("/") || name.isBlank()) {
            throw new IllegalArgumentException("Invalid node name");
        }
        this.name = name;
    }
    @Override
    public String getName() {
        return name;
    }
}