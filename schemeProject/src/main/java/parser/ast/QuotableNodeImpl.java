package parser.ast;

abstract class QuotableNodeImpl implements QuotableNode{
    boolean quoted = false;

    @Override
    public void setQuoted() {
        this.quoted = true;
    }

    @Override
    public boolean isQuoted() {
        return quoted;
    }
}
