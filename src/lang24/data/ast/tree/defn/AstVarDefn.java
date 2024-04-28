package lang24.data.ast.tree.defn;

import lang24.common.report.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * A definition of a variable.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstVarDefn extends AstDefn {

	/**
	 * Constructs a definition of a variable.
	 * 
	 * @param location The location.
	 * @param name     The name of the this variable.
	 * @param type     The type of the this variable.
	 */
	public AstVarDefn(final Locatable location, final String name, final AstType type) {
		super(location, name, type);
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}