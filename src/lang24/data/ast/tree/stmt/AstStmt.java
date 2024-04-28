package lang24.data.ast.tree.stmt;

import lang24.common.report.*;
import lang24.data.ast.tree.*;

/**
 * A statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class AstStmt extends AstNode {

	/**
	 * Constructs a statement.
	 * 
	 * @param location The location.
	 */
	public AstStmt(final Locatable location) {
		super(location);
	}

}