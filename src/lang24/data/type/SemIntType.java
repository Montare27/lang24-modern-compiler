package lang24.data.type;

import lang24.data.type.visitor.*;

/**
 * Integer type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemIntType extends SemAtomType {

	/** An object of {@code SemIntType} class. */
	public static final SemIntType type = new SemIntType();

	/** Constructs a new integer type. */
	private SemIntType() {
	}

	@Override
	public String toString() {
		return "integer";
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}