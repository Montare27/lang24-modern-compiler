package lang24.data.type;

import lang24.data.type.visitor.*;

/**
 * Character type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemCharType extends SemAtomType {

	/** An object of {@code SemCharType} class. */
	public static final SemCharType type = new SemCharType();

	/** Constructs a new charater type. */
	private SemCharType() {
	}

	@Override
	public String toString() {
		return "char";
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}