package lang24.data.ast.attribute;

import java.util.*;
import lang24.data.ast.tree.*;

/**
 * An attribute of the abstract syntax tree node.
 *
 * @param <Node>  Nodes that values are associated with.
 * @param <Value> Values associated with nodes.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class Attribute<Node extends AstNode, Value> {

	/** Mapping of nodes to values. */
	private final Vector<Value> mapping;

	/**
	 * Constructs a new attribute.
	 */
	public Attribute() {
		this.mapping = new Vector<Value>();
	}

	/**
	 * Associates a value with the specified abstract syntax tree node.
	 * 
	 * @param node  The specified abstract syntax tree node.
	 * @param value The value.
	 * @return The value.
	 */
	public Value put(final Node node, final Value value) {
		int id = node.id();
		while (id >= mapping.size())
			mapping.setSize(id + 1000);
		mapping.set(id, value);
		return value;
	}

	/**
	 * Returns a value associated with the specified abstract syntax tree node.
	 * 
	 * @param node The specified abstract syntax tree node.
	 * @return The value (or {@code null} if the value is not found).
	 */
	public Value get(final Node node) {
		int id = node.id();
		while (id >= mapping.size())
			return null;
		return mapping.get(id);
	}

}