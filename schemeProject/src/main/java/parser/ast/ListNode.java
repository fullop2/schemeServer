package parser.ast;


public interface ListNode extends QuotableNode {

    static ListNode EMPTYLIST = new ListNode() {
        @Override
        public Node car() {
            return null;
        }

        @Override
        public ListNode cdr() {
            return null;
        }

        @Override
        public ListNode setQuotedIn(){
            return ListNode.cons(setQuotedInside(this.car()), (ListNode)setQuotedInside(this.cdr()));
        }

        @Override
        public Node setQuotedInside(Node node){
            if (node instanceof QuotableNode) { // case QuatableNode
                ((QuotableNode) node).setQuoted();
                return node;
            }
            else if(node instanceof ListNode) { // case ListNode
                setQuotedInside(((ListNode) node).car());
                setQuotedInside(((ListNode) node).cdr());
            }
            return node;
        }
        @Override
        public void setQuoted(){ }
        @Override
        public boolean isQuoted(){
            return false;
        }
    };
    static ListNode ENDLIST = new ListNode() {

        @Override
        public Node car() {
            return null;
        }

        @Override
        public ListNode cdr() {
            return null;
        }

        @Override
        public ListNode setQuotedIn(){
            return ListNode.cons(setQuotedInside(this.car()), (ListNode)setQuotedInside(this.cdr()));
        }

        @Override
        public Node setQuotedInside(Node node){
        	if(node instanceof ListNode) { // case ListNode
                setQuotedInside(((ListNode) node).car());
                setQuotedInside(((ListNode) node).cdr());
            }
        	else if (node instanceof QuotableNode) { // case QuatableNode
                ((QuotableNode) node).setQuoted();
                return node;
            }
            return node;
        }

        @Override
        public void setQuoted(){ }
        @Override
        public boolean isQuoted(){
            return false;
        }
    };

    static ListNode cons(Node head, ListNode tail) {
        return new ListNode() {
            @Override
            public Node car() {
                return head;
            }

            @Override
            public ListNode cdr() {
                return tail;
            }

            @Override
            public ListNode setQuotedIn(){
                return ListNode.cons(setQuotedInside(this.car()), (ListNode)setQuotedInside(this.cdr()));
            }

            @Override
            public Node setQuotedInside(Node node){
                if(node instanceof ListNode) { // case ListNode
                    setQuotedInside(((ListNode) node).car());
                    setQuotedInside(((ListNode) node).cdr());
                }
                else if (node instanceof QuotableNode) { // case QuatableNode
                    ((QuotableNode) node).setQuoted();
                    return node;
                }
                return node;
            }

            public void setQuoted(){}
            @Override
            public boolean isQuoted(){
                Node head = this.car();
                while(head instanceof ListNode){
                    head = ((ListNode) head).car();
                }
                return ((QuotableNodeImpl)head).isQuoted();
            }
        };
    }

    Node car();
    ListNode cdr();
    ListNode setQuotedIn();
    Node setQuotedInside(Node node);
}

