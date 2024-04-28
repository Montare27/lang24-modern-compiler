package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A binary expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstBinExpr extends AstExpr {

	/** Operators. */
	public enum Oper {
		/** Logical or. */
		OR,
		/** Logical and. */
		AND,
		/** Equal. */
		EQU,
		/** Not equal. */
		NEQ,
		/** Less than. */
		LTH,
		/** Greater than. */
		GTH,
		/** Less of equal. */
		LEQ,
		/** Greater or equal. */
		GEQ,
		/** Add. */
		ADD,
		/** Subtract. */
		SUB,
		/** Multiply. */
		MUL,
		/** Divide. */
		DIV,
		/** Modulo. */
		MOD,
	};

	/** The operator. */
	public final Oper oper;

	/** The first subexpression. */
	public final AstExpr fstExpr;

	/** The second subexpression. */
	public final AstExpr sndExpr;

	/**
	 * Constructs a binary expression.
	 * 
	 * @param location The location.
	 * @param oper     The operator.
	 * @param fstExpr  The first subexpression.
	 * @param sndExpr  The second subexpression.
	 */
	public AstBinExpr(Locatable location, Oper oper, AstExpr fstExpr, AstExpr sndExpr) {
		super(location);
		this.oper = oper;
		this.fstExpr = fstExpr;
		this.sndExpr = sndExpr;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}