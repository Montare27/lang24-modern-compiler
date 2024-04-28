package lang24.data.type;

import lang24.data.type.visitor.*;

/**
 * Array type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemArrayType extends SemValueType {

	/** The type of elements in this array. */
	public final SemType elemType;

	/** The size of this array. */
	public final long size;

	/**
	 * Constructs an array type.
	 * 
	 * @param elemType The type of elements in this array.
	 * @param size     The size of this array.
	 */
	public SemArrayType(final SemType elemType, final long size) {
		this.elemType = elemType;
		this.size = size;
	}

	@Override
	public String toString() {
		return "array[" + size + "]" + elemType.toString();
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}