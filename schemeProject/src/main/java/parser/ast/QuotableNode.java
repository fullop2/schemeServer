package parser.ast;

public interface QuotableNode extends Node {
    public void setQuoted();
    public boolean isQuoted();
}
