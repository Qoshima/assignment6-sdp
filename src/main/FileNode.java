public class FileNode extends AbstractNode {
    private final long size;
    public long getSize() {return size;}
    public FileNode(String name, long size) {
        super(name);
        if (size < 0) {
            throw new IllegalArgumentException("Invalid node size");
        }
        this.size = size;
    }
    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
