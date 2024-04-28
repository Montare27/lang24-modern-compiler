package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A prefix expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstPfxExpr extends AstExpr {

	/** Operators. */
	public enum Oper {
		/** Sign. */
		ADD,
		/** Sign. */
		SUB,
		/** Negation. */
		NOT,
		/** Reference. */
		PTR,
	};

	/** The operator. */
	public final Oper oper;

	/** The subexpression. */
	public final AstExpr expr;

	/**
	 * Constructs a prefix expression.
	 * 
	 * @param location The location.
	 * @param oper     The operator.
	 * @param expr     The subexpression.
	 */
	public AstPfxExpr(Locatable location, Oper oper, AstExpr expr) {
		super(location);
		this.oper = oper;
		this.expr = expr;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}