package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.tree.*;

/**
 * A type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class AstType extends AstNode {

	/**
	 * Constructs a type.
	 * 
	 * @param location The location.
	 */
	public AstType(final Locatable location) {
		super(location);
	}

}