abstract class AbstractNode implements Element {
    private final String name;
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