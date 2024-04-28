package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * An array access expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstArrExpr extends AstExpr {

	/** The array. */
	public final AstExpr arr;

	/** The index. */
	public final AstExpr idx;

	/**
	 * Constructs an array access expression.
	 * 
	 * @param location The location.
	 * @param arr      The array.
	 * @param idx      The index.
	 */
	public AstArrExpr(Locatable location, AstExpr arr, AstExpr idx) {
		super(location);
		this.arr = arr;
		this.idx = idx;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}