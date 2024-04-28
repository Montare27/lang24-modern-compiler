package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * A {@code sizeof} expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstSizeofExpr extends AstExpr {

	/** The type. */
	public final AstType type;

	/**
	 * Constructs a {code sizeof} expression.
	 * 
	 * @param location The location.
	 * @param type     The type.
	 */
	public AstSizeofExpr(Locatable location, AstType type) {
		super(location);
		this.type = type;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}