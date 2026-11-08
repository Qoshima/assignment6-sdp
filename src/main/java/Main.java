public class Main {
    public static void main(String[] args) {

        //Create file system
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode archive = new DirectoryNode("archive");

        root.add(new FileNode("readme.txt", 100));
        root.add(new FileNode("report-final.pdf", 1200));

        documents.add(new FileNode("report-draft.pdf", 600));
        documents.add(new FileNode("photo.png", 800));

        root.add(documents);
        root.add(archive);

        //Total Size Visitor
        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);

        System.out.println("\nTotal size");
        System.out.println("Total size: " +
                sizeVisitor.getTotalSize() + " bytes");

        //Search Visitor
        SearchVisitor searchVisitor = new SearchVisitor("report", 600);
        searchVisitor.search(root);

        System.out.println("\nSearch");
        System.out.println("Name contains: report");
        System.out.println("Minimum size: 600 bytes");

        for (String path : searchVisitor.getMatches()) {
            System.out.println(path);
        }

        //Statistics Visitor
        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        statsVisitor.analyze(root);

        System.out.println("\nStatistics");
        System.out.println("Files: " + statsVisitor.getFileCount());
        System.out.println("Directories: " + statsVisitor.getDirectoryCount());
        System.out.println("Maximum depth: " + statsVisitor.getMaxDepth());

        // Search from a different root
        searchVisitor.search(documents);

        System.out.println("\nSearch from documents");

        for (String path : searchVisitor.getMatches()) {
            System.out.println(path);
        }
    }
}
