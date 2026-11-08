# Visitor Pattern - Advanced File System

Java project for Assignment 6 in Software Design Patterns. It models an in-memory file tree and uses three visitors to calculate file sizes, search for files, and collect tree statistics.

## Requirements

- Java 17
- Maven

## Run tests

```bash
mvn test
```

## Run the program

```bash
mvn compile
java -cp target/classes Main
```

`Main` creates a sample file tree and runs all three visitors. It also repeats a search from a nested directory to demonstrate root-relative paths.

## Design

The visitors handle traversal. When a visitor reaches a directory, it calls `accept(this)` on each child. Files and directories do not contain the size, search, or statistics operations.

A new operation can be added with another visitor without changing `FileNode` or `DirectoryNode`. Adding a new element type would require updating the `Visitor` interface and existing visitors.

`SearchVisitor.search()` and `StatisticsVisitor.analyze()` clear their previous results. `TotalSizeVisitor` can be reused by calling `reset()` before another traversal.
