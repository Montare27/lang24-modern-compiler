package lang24.data.type;

import lang24.data.type.visitor.SemVisitor;

/**
 * Void type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemVoidType extends SemType {

	/** An object of {@code SemVoidType} class. */
	public static final SemVoidType type = new SemVoidType();

	/** Constructs a void type. */
	private SemVoidType() {
	}

	@Override
	public String toString() {
		return "void";
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}