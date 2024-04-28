package lang24.data.type;

import lang24.data.type.visitor.*;

/**
 * Boolean type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemBoolType extends SemAtomType {

	/** An object of {@code SemBoolType} class. */
	public static final SemBoolType type = new SemBoolType();

	/** Constructs a new boolean type. */
	private SemBoolType() {
	}

	@Override
	public String toString() {
		return "boolean";
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}