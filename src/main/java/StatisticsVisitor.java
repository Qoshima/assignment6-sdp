public class StatisticsVisitor implements Visitor {
    private int fileCount;
    private int directoryCount;
    private int maxDepth;
    private int currentDepth;

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }


    @Override
    public void visit(FileNode file) {
        fileCount++;
        maxDepth = Math.max(maxDepth, currentDepth);
    }

    @Override
    public void visit(DirectoryNode directory) {
        directoryCount++;
        maxDepth = Math.max(maxDepth, currentDepth);
        for (Element child : directory.getChildren()) {
            currentDepth++;
            child.accept(this);
            currentDepth--;
        }
    }

    public void analyze(DirectoryNode root) {
        if (root == null) {
            throw new IllegalArgumentException("Cannot analyze null root");
        }
        currentDepth = 0;
        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
        root.accept(this);
    }
}
