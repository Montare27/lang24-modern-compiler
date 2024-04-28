package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * An if statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstIfStmt extends AstStmt {

	/** The condition. */
	public final AstExpr cond;

	/** The statement in the then branch. */
	public final AstStmt thenStmt;

	/** The statement in the else branch. */
	public final AstStmt elseStmt;

	/**
	 * Constructs an if statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param thenStmt The statement in the then branch.
	 * @param elseStmt The statement in the else branch.
	 */
	public AstIfStmt(Locatable location, AstExpr cond, AstStmt thenStmt, AstStmt elseStmt) {
		super(location);
		this.cond = cond;
		this.thenStmt = thenStmt;
		this.elseStmt = elseStmt;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}