package lang24.data.ast.tree;

import java.util.*;
import java.util.function.*;
import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * A sequence of nodes of an abstract syntax tree.
 * 
 * @param <Node> The type of nodes stored in this sequence.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstNodes<Node extends AstNode> extends AstNode implements Iterable<Node> {

	/** The nodes stored in this sequence. */
	private final Node[] nodes;

	/**
	 * Constructs a sequence of nodes.
	 */
	public AstNodes() {
		this(new Location(0, 0), new Vector<Node>());
	}

	/**
	 * Constructs a sequence of nodes.
	 * 
	 * @param nodes The nodes stored in this sequence.
	 */
	@SuppressWarnings("unchecked")
	public AstNodes(final List<Node> nodes) {
		super(nodes.isEmpty() ? new Location(0, 0) : new Location(nodes.getFirst(), nodes.getLast()));
		this.nodes = (Node[]) (new AstNode[nodes.size()]);
		int index = 0;
		for (final Node n : nodes)
			this.nodes[index++] = n;
		if (this.nodes.length == 0)
			relocate(new Location(0, 0));
		else
			relocate(new Location(this.nodes[0], this.nodes[this.nodes.length - 1]));
	}

	/**
	 * Constructs a sequence of nodes.
	 * 
	 * @param location The location.
	 * @param nodes    The nodes stored in this sequence.
	 */
	@SuppressWarnings("unchecked")
	public AstNodes(final Locatable location, final List<Node> nodes) {
		super(location);
		this.nodes = (Node[]) (new AstNode[nodes.size()]);
		int index = 0;
		for (final Node n : nodes)
			this.nodes[index++] = n;
	}

	/**
	 * Returns the node at the specified position in this sequence.
	 * 
	 * @param index The index of the node to return.
	 * @return The node at the specified index.
	 */
	public final Node get(final int index) {
		return nodes[index];
	}

	/**
	 * Returns the number of nodes in this sequence.
	 * 
	 * @return The number of nodes in this sequence.
	 */
	public final int size() {
		return nodes.length;
	}

	// Iterable<Node>

	@Override
	public void forEach(final Consumer<? super Node> action) throws NullPointerException {
		for (final Node n : this)
			action.accept(n);
	}

	@Override
	public Iterator<Node> iterator() {
		return new NodesIterator();
	}

	// Iterator.

	/**
	 * Iterator over nodes with the removal operation blocked.
	 * 
	 * It is assumed that the underlying array of nodes is immutable.
	 */
	private final class NodesIterator implements Iterator<Node> {

		/** Constructs a new iterator. */
		private NodesIterator() {
		}

		/** The index of the next node to be returned. */
		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < nodes.length;
		}

		@Override
		public Node next() throws NoSuchElementException {
			if (index < nodes.length)
				return nodes[index++];
			else
				throw new NoSuchElementException("");
		}

		@Override
		public void remove() {
			throw new Report.InternalError();
		}

		@Override
		public void forEachRemaining(final Consumer<? super Node> action) {
			while (hasNext())
				action.accept(next());
		}

	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}