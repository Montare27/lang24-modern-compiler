package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * An expression statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstExprStmt extends AstStmt {

	/** The expression. */
	public final AstExpr expr;

	/**
	 * Constructs an expression statement.
	 * 
	 * @param location The location.
	 * @param expr     The expression.
	 */
	public AstExprStmt(Locatable location, AstExpr expr) {
		super(location);
		this.expr = expr;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}