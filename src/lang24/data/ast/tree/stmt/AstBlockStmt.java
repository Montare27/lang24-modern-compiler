package lang24.data.ast.tree.stmt;

import java.util.*;
import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.visitor.*;

/**
 * A block statement.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstBlockStmt extends AstStmt {

	/** A sequence of statements inside this block statement. */
	public final AstNodes<AstStmt> stmts;

	/**
	 * Constructs a block statement.
	 * 
	 * @param location The location.
	 * @param stmts    The sequence of statements inside this block statement.
	 */
	public AstBlockStmt(final Locatable location, final List<AstStmt> stmts) {
		super(location);
		this.stmts = new AstNodes<AstStmt>(stmts);
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}