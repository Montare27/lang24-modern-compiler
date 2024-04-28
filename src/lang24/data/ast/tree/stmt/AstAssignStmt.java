package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * An assignment statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstAssignStmt extends AstStmt {

	/** The destination. */
	public final AstExpr dst;

	/** The source. */
	public final AstExpr src;

	/**
	 * Construct an assignment statement.
	 * 
	 * @param location The location.
	 * @param dst      The destination.
	 * @param src      The source.
	 */
	public AstAssignStmt(Locatable location, AstExpr dst, AstExpr src) {
		super(location);
		this.dst = dst;
		this.src = src;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}