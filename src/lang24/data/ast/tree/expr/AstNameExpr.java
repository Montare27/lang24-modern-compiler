package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A function call.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstNameExpr extends AstExpr {

	/** The name. */
	public String name;

	/**
	 * Constructs a function call.
	 * 
	 * @param location The location.
	 * @param name     The name.
	 */
	public AstNameExpr(Locatable location, String name) {
		super(location);
		this.name = name;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}