package lang24.data.type;

import java.util.*;
import java.util.function.*;
import lang24.common.report.*;

/**
 * A sequence of types.
 * 
 * @param <Type> The type of types stored in this sequence.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemTypes<Type extends SemType> implements Iterable<Type> {

	/** The types stored in this sequence. */
	private final Type[] types;

	/**
	 * Constructs a sequence of types.
	 */
	public SemTypes() {
		this(new Vector<Type>());
	}

	/**
	 * Constructs a sequence of types.
	 * 
	 * @param types The types stored in this sequence.
	 */
	@SuppressWarnings("unchecked")
	public SemTypes(final List<Type> types) {
		this.types = (Type[]) (new SemType[types.size()]);
		int index = 0;
		for (final Type t : types)
			this.types[index++] = t;
	}

	/**
	 * Returns the type at the specified position in this sequence.
	 * 
	 * @param index The index of the type to return.
	 * @return The type at the specified index.
	 */
	public final Type get(final int index) {
		return types[index];
	}

	/**
	 * Returns the number of types in this sequence.
	 * 
	 * @return The number of types in this sequence.
	 */
	public final int size() {
		return types.length;
	}

	// Iterable<Node>

	@Override
	public void forEach(final Consumer<? super Type> action) throws NullPointerException {
		for (final Type t : this)
			action.accept(t);
	}

	@Override
	public Iterator<Type> iterator() {
		return new TypesIterator();
	}

	// Iterator.

	/**
	 * Iterator over types with the removal operation blocked.
	 * 
	 * It is assumed that the underlying array of types is immutable.
	 */
	private final class TypesIterator implements Iterator<Type> {

		/** Constructs a new iterator. */
		private TypesIterator() {
		}

		/** The index of the next type to be returned. */
		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < types.length;
		}

		@Override
		public Type next() throws NoSuchElementException {
			if (index < types.length)
				return types[index++];
			else
				throw new NoSuchElementException("");
		}

		@Override
		public void remove() {
			throw new Report.InternalError();
		}

		@Override
		public void forEachRemaining(final Consumer<? super Type> action) {
			while (hasNext())
				action.accept(next());
		}

	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer();
		str.append("(");
		boolean fst = true;
		for (SemType type : types) {
			if (!fst)
				str.append(",");
			str.append(type.toString());
			fst = false;
		}
		str.append(")");
		return str.toString();
	}

}