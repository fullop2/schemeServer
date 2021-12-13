package parser.ast;

public class BooleanNode extends QuotableNodeImpl implements ValueNode {
    public static BooleanNode FALSE_NODE = new BooleanNode(false);
    public static BooleanNode TRUE_NODE = new BooleanNode(true);
    Boolean value;

    private BooleanNode(Boolean b) {
        value = b;
    }

    @Override
    public String toString() {
        return value ? "#T" : "#F";
    }

}
