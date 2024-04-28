package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * A return statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstReturnStmt extends AstStmt {

	/** The return value. */
	public final AstExpr expr;

	/**
	 * Constructs a return statement.
	 * 
	 * @param location The location.
	 * @param expr     The return value.
	 */
	public AstReturnStmt(Locatable location, AstExpr expr) {
		super(location);
		this.expr = expr;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}