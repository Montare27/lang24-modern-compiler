package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.visitor.*;

/**
 * A union type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstUniType extends AstRecType {

	/**
	 * Constructs a union type.
	 * 
	 * @param location The location.
	 * @param cmps     The components of this union.
	 */
	public AstUniType(final Location location, final AstNodes<AstCmpDefn> cmps) {
		super(location, cmps);
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}