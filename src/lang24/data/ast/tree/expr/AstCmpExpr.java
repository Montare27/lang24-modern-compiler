package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A component access expression.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstCmpExpr extends AstExpr {

	/** The container. */
	public final AstExpr expr;

	/** The component name. */
	public final String name;

	/**
	 * Constructs a component access expression.
	 * 
	 * @param location The location.
	 * @param expr     The container.
	 * @param name     The component name.
	 */
	public AstCmpExpr(Locatable location, AstExpr expr, String name) {
		super(location);
		this.expr = expr;
		this.name = name;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}