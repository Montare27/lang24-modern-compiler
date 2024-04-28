package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.visitor.*;

/**
 * A struct type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstStrType extends AstRecType {

	/**
	 * Constructs a struct type.
	 * 
	 * @param location The location.
	 * @param cmps     The components of this struct.
	 */
	public AstStrType(final Location location, final AstNodes<AstCmpDefn> cmps) {
		super(location, cmps);
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}