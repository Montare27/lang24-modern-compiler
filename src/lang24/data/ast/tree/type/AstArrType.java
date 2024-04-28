package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.*;

/**
 * An array type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstArrType extends AstType {

	/** The type of elements in this array. */
	public final AstType elemType;

	/** The number of elements. */
	public final AstExpr size;

	/**
	 * Constructs an array type.
	 * 
	 * @param location The location.
	 * @param elemType The type of elements in this array.
	 * @param size     The number of elements.
	 */
	public AstArrType(final Location location, final AstType elemType, final AstExpr size) {
		super(location);
		this.elemType = elemType;
		this.size = size;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}