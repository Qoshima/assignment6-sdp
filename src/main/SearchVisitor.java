import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchVisitor implements Visitor {
    private final String nameFilter;
    private final long minSizeFilter;
    private final List<String> matches;
    private DirectoryNode searchRoot;

    public SearchVisitor(String nameFilter, long minSizeFilter) {
        if (nameFilter == null) {
            throw new IllegalArgumentException("name filter cannot be null");
        }
        if (minSizeFilter < 0) {
            throw new IllegalArgumentException("min size cannot be negative");
        }
        this.nameFilter = nameFilter.toLowerCase();
        this.minSizeFilter = minSizeFilter;
        this.matches = new ArrayList<>();
    }

    @Override
    public void visit(FileNode file) {
        if (file.getName().toLowerCase().contains(nameFilter) && file.getSize() >= minSizeFilter) {
            matches.add(buildPath(file));
        }
    }
    @Override
    public void visit(DirectoryNode directory) {
        for (Element child : directory.getChildren()) {
            child.accept(this);
        }
    }

    private String buildPath(FileNode file) {
        List<String> parts = new ArrayList<>();
        parts.add(file.getName());

        DirectoryNode current = file.getParent();

        while (current != null && searchRoot != current) {

            parts.add(current.getName());
            current = current.getParent();
        }
        Collections.reverse(parts);
        return String.join("/", parts);
    }

    public void search(DirectoryNode root) {
        if (root == null) {
            throw new IllegalArgumentException("root cannot be null");
        }
        matches.clear();
        searchRoot = root;
        root.accept(this);
    }

    public List<String> getMatches() {
        List<String> result = new ArrayList<>(matches);
        Collections.sort(result);
        return List.copyOf(result);
    }
}