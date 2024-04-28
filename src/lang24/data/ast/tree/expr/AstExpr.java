package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.tree.*;

/**
 * An expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class AstExpr extends AstNode {

	/**
	 * Constructs an expression.
	 * 
	 * @param location The location.
	 */
	public AstExpr(final Locatable location) {
		super(location);
	}

}