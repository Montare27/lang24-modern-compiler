package lang24.data.ast.tree.defn;

import lang24.common.report.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * A definition of a type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstTypDefn extends AstDefn {

	/**
	 * Constructs a definition of a type.
	 * 
	 * @param location The location.
	 * @param name     The name of the this type.
	 * @param type     The type of the this type.
	 */
	public AstTypDefn(final Locatable location, final String name, final AstType type) {
		super(location, name, type);
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}