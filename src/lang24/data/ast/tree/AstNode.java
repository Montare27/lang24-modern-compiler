package lang24.data.ast.tree;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A node of an abstract syntax tree.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class AstNode implements Locatable {

	/** The number of nodes constructed so far. */
	private static int numNodes = 0;

	/** The unique id of this node. */
	public final int id;

	/** The location of this node. */
	private Location location;

	/**
	 * Constructs a node of an abstract syntax tree.
	 * 
	 * @param location The location.
	 */
	public AstNode(final Locatable location) {
		id = numNodes++;
		this.location = location.location();
	}

	/**
	 * Returns the unique id of this node.
	 * 
	 * @return The unique id of this node.
	 */
	public final int id() {
		return id;
	}

	@Override
	public final void relocate(final Locatable location) {
		this.location = location.location();
	}

	@Override
	public final Location location() {
		return location;
	}

	/**
	 * The acceptor method.
	 * 
	 * @param <Result>   The result type.
	 * @param <Argument> The argument type.
	 * @param visitor    The visitor accepted by this acceptor.
	 * @param arg        The argument.
	 * @return The result.
	 */
	public abstract <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg);

}