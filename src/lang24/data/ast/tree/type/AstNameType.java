package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A type name.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstNameType extends AstType {

	/** The name. */
	public final String name;

	/**
	 * Constructs a type name.
	 * 
	 * @param location The location.
	 * @param name     The name of type.
	 */
	public AstNameType(final Locatable location, final String name) {
		super(location);
		this.name = name;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}