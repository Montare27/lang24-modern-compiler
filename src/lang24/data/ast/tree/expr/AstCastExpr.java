package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * A cast expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstCastExpr extends AstExpr {

	/** The type. */
	public final AstType type;

	/** The expression. */
	public final AstExpr expr;

	/**
	 * Constructs a casr expression.
	 * 
	 * @param location The location.
	 * @param type     The type.
	 * @param expr     The expression.
	 */
	public AstCastExpr(Locatable location, AstType type, AstExpr expr) {
		super(location);
		this.type = type;
		this.expr = expr;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}