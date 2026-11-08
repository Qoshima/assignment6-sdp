import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileSystemTest {

    // 1. Empty directory is still counted as one directory. T6 - empty structure
    @Test
    void emptyDirectoryStatistics() {
        DirectoryNode root = new DirectoryNode("root");
        StatisticsVisitor stats = new StatisticsVisitor();

        stats.analyze(root);

        assertEquals(0, stats.getFileCount());
        assertEquals(1, stats.getDirectoryCount());
        assertEquals(0, stats.getMaxDepth());
    }

    // 2. Files and directories are counted exactly once; depth starts at zero. T5 - Statistics and depth
    @Test
    void nestedTreeStatistics() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode documents = new DirectoryNode("documents");
        DirectoryNode images = new DirectoryNode("images");

        root.add(new FileNode("a.txt", 10));
        root.add(documents);
        documents.add(new FileNode("b.txt", 20));
        documents.add(images);
        images.add(new FileNode("photo.png", 30));

        StatisticsVisitor stats = new StatisticsVisitor();
        stats.analyze(root);

        assertEquals(3, stats.getFileCount());
        assertEquals(3, stats.getDirectoryCount());
        assertEquals(3, stats.getMaxDepth());
    }

    // 3. An empty nested directory can define the maximum depth.
    @Test
    void emptyNestedDirectoryAffectsDepth() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = new DirectoryNode("b");
        root.add(a);
        a.add(b);

        StatisticsVisitor stats = new StatisticsVisitor();
        stats.analyze(root);

        assertEquals(0, stats.getFileCount());
        assertEquals(3, stats.getDirectoryCount());
        assertEquals(2, stats.getMaxDepth());
    }

    // 4. A second analysis must not include results from the first one. T7 - repeated execution for analyze
    @Test
    void statisticsAreResetBetweenRuns() {
        DirectoryNode first = new DirectoryNode("first");
        first.add(new FileNode("a.txt", 5));
        DirectoryNode second = new DirectoryNode("second");
        StatisticsVisitor stats = new StatisticsVisitor();

        stats.analyze(first);
        assertEquals(1, stats.getFileCount());
        assertEquals(1, stats.getMaxDepth());

        stats.analyze(second);
        assertEquals(0, stats.getFileCount());
        assertEquals(1, stats.getDirectoryCount());
        assertEquals(0, stats.getMaxDepth());
    }

    // 5. Sizes include nested files and zero-byte files without double counting. T2 - total size
    @Test
    void totalSizeOfNestedTree() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode nested = new DirectoryNode("nested");
        root.add(new FileNode("a.bin", 100));
        root.add(nested);
        nested.add(new FileNode("b.bin", 40));
        nested.add(new FileNode("empty.bin", 0));

        TotalSizeVisitor visitor = new TotalSizeVisitor();
        root.accept(visitor);

        assertEquals(140L, visitor.getTotalSize());
    }

    // 6. The current TotalSizeVisitor API requires an explicit reset before reuse. T7 - repeated execution
    @Test
    void totalSizeCanBeReset() {
        DirectoryNode first = new DirectoryNode("first");
        first.add(new FileNode("first.txt", 5));
        DirectoryNode second = new DirectoryNode("second");
        second.add(new FileNode("second.txt", 8));
        TotalSizeVisitor visitor = new TotalSizeVisitor();

        first.accept(visitor);
        assertEquals(5L, visitor.getTotalSize());

        visitor.reset();
        second.accept(visitor);
        assertEquals(8L, visitor.getTotalSize());
    }

    // 7. Name and size criteria both apply; the minimum size is inclusive. T3 - search filters
    @Test
    void searchUsesBothFiltersAndInclusiveMinimum() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("Report-A.txt", 500));
        root.add(new FileNode("Report-B.txt", 499));
        root.add(new FileNode("notes.txt", 1000));
        root.add(new FileNode("report-C.txt", 900));

        SearchVisitor search = new SearchVisitor("REPORT", 500);
        search.search(root);

        assertEquals(List.of("Report-A.txt", "report-C.txt"), search.getMatches());
    }

    // 8. Paths are relative to the search root and lexicographically sorted. T4 - relative paths and sorting
    @Test
    void searchReturnsSortedRelativePaths() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode docs = new DirectoryNode("docs");
        root.add(new FileNode("z-log.txt", 10));
        root.add(docs);
        docs.add(new FileNode("a-log.txt", 20));

        SearchVisitor search = new SearchVisitor("log", 0);
        search.search(root);

        assertEquals(List.of("docs/a-log.txt", "z-log.txt"), search.getMatches());
    }

    // 9. Changing the search root changes the relative path.
    @Test
    void searchRootCanBeNestedDirectory() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode university = new DirectoryNode("university");
        DirectoryNode semester = new DirectoryNode("semester");
        root.add(university);
        university.add(semester);
        semester.add(new FileNode("report.pdf", 1500));

        SearchVisitor search = new SearchVisitor("report", 0);
        search.search(root);
        assertEquals(List.of("university/semester/report.pdf"), search.getMatches());

        search.search(university);
        assertEquals(List.of("semester/report.pdf"), search.getMatches());

        search.search(semester);
        assertEquals(List.of("report.pdf"), search.getMatches());
    }

    // 10. Search results do not accumulate across repeated runs. T7
    @Test
    void searchResultsAreResetBetweenRuns() {
        DirectoryNode first = new DirectoryNode("first");
        DirectoryNode second = new DirectoryNode("second");
        first.add(new FileNode("one.txt", 1));
        second.add(new FileNode("two.txt", 2));

        SearchVisitor search = new SearchVisitor(".txt", 0);
        search.search(first);
        assertEquals(List.of("one.txt"), search.getMatches());

        search.search(second);
        assertEquals(List.of("two.txt"), search.getMatches());
    }

    // 11. The caller cannot modify the returned search results.
    @Test
    void searchResultsAreImmutable() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("a.txt", 1));
        SearchVisitor search = new SearchVisitor(".txt", 0);
        search.search(root);

        List<String> result = search.getMatches();
        assertThrows(UnsupportedOperationException.class, () -> result.add("fake.txt"));
        assertEquals(List.of("a.txt"), search.getMatches());
    }

    // 12. Non-matching and empty searches return an empty result. T6 - no matches
    @Test
    void searchWithNoMatches() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("image.png", 10));
        SearchVisitor search = new SearchVisitor("report", 0);

        search.search(root);
        assertEquals(List.of(), search.getMatches());
    }

    // 13. Negative file sizes are invalid; zero is allowed. T8 - tree integrity
    @Test
    void fileSizeValidation() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("bad.txt", -1));
        assertEquals(0L, new FileNode("empty.txt", 0).getSize());
    }

    // 14. Two children of the same directory cannot have the same name. T8 - tree integrity
    @Test
    void duplicateSiblingNamesAreRejected() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("data", 10));

        assertThrows(IllegalArgumentException.class,
                () -> root.add(new DirectoryNode("data")));
    }

    // 15. The same child name in different directories is valid.
    @Test
    void sameNameInDifferentDirectoriesIsAllowed() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = new DirectoryNode("b");
        root.add(a);
        root.add(b);
        a.add(new FileNode("report.txt", 1));
        b.add(new FileNode("report.txt", 2));

        StatisticsVisitor stats = new StatisticsVisitor();
        stats.analyze(root);
        assertEquals(2, stats.getFileCount());
    }

    // 16. One child cannot belong to two different parents. T8 - tree integrity
    @Test
    void multipleParentsAreRejected() {
        DirectoryNode a = new DirectoryNode("a");
        DirectoryNode b = new DirectoryNode("b");
        FileNode shared = new FileNode("shared.txt", 10);
        a.add(shared);

        assertThrows(IllegalStateException.class, () -> b.add(shared));
    }

    // 17. Neither self-cycles nor ancestor cycles are allowed. T8 - tree integrity
    @Test
    void cyclesAreRejected() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode child = new DirectoryNode("child");
        root.add(child);

        assertThrows(IllegalArgumentException.class, () -> root.add(root));
        assertThrows(IllegalArgumentException.class, () -> child.add(root));
    }

    // 18. Child collections cannot be modified from outside.
    @Test
    void childrenCollectionIsImmutable() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("a.txt", 1));

        List<Element> children = root.getChildren();
        assertThrows(UnsupportedOperationException.class,
                () -> children.add(new FileNode("b.txt", 2)));
        assertEquals(1, root.getChildren().size());
    }

    // 19. Invalid names are rejected by AbstractNode's constructor.
    @Test
    void invalidNodeNamesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("", 1));
        assertThrows(IllegalArgumentException.class, () -> new FileNode("a/b", 1));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode("  "));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(null));
    }

    // 20. Invalid inputs to visitor APIs fail predictably.
    @Test
    void invalidVisitorArgumentsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor(null, 0));
        assertThrows(IllegalArgumentException.class, () -> new SearchVisitor("a", -1));
        assertThrows(IllegalArgumentException.class,
                () -> new SearchVisitor("a", 0).search(null));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticsVisitor().analyze(null));
    }

    // 21. Both element types call the correct visit method. T1 - Double Dispatch
    @Test
    void doubleDispatchWorks() {
        FileNode file = new FileNode("test.txt", 100);
        DirectoryNode directory = new DirectoryNode("root");

        int[] calls = {0, 0};

        Visitor visitor = new Visitor() {
            @Override
            public void visit(FileNode file) {
                calls[0]++;
            }

            @Override
            public void visit(DirectoryNode directory) {
                calls[1]++;
            }
        };

        file.accept(visitor);
        directory.accept(visitor);

        assertEquals(1, calls[0]);
        assertEquals(1, calls[1]);
    }
}
