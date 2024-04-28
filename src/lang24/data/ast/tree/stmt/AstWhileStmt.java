package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * A while statement.
 *
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstWhileStmt extends AstStmt {

	/** The condition. */
	public final AstExpr cond;

	/** The inner statement. */
	public final AstStmt stmt;

	/**
	 * Constructs a while statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param body     The inner statement.
	 */
	public AstWhileStmt(Locatable location, AstExpr cond, AstStmt body) {
		super(location);
		this.cond = cond;
		this.stmt = body;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}