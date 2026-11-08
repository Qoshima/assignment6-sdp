public class TotalSizeVisitor implements Visitor {
    private long totalSize = 0;

    public long getTotalSize() {
        return totalSize;
    }

    @Override
    public void visit(FileNode file) {
        totalSize += file.getSize();
    }
    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }
    public void reset() {
        totalSize = 0;
    }
}
