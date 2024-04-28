package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A pointer type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstPtrType extends AstType {

	/** The base type pointers of this type point to. */
	public final AstType baseType;

	/**
	 * Constructs a pointer type.
	 * 
	 * @param location The location.
	 * @param baseType The base type pointers of this type point to.
	 */
	public AstPtrType(final Locatable location, final AstType baseType) {
		super(location);
		this.baseType = baseType;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}