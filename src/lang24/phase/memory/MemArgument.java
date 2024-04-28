package lang24.phase.memory;

import lang24.data.ast.tree.defn.AstFunDefn;

public class MemArgument {
    public long depth;
    public long offset;
    public int precedence;
    public AstFunDefn parentFunction;
    public Object more;

    public MemArgument(long depth, long offset) {
        this.depth = depth;
        this.offset = offset;
    }

    public MemArgument(long depth, long offset, Object more) {
        this(depth, offset);
        this.more = more;
    }

    public MemArgument copy() {
        MemArgument memArgument = new MemArgument(this.depth, this.offset, this.more);
        memArgument.precedence = this.precedence;
        memArgument.parentFunction = this.parentFunction;
        return memArgument;
    }
}
