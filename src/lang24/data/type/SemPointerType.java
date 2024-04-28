package lang24.data.type;

import lang24.common.report.*;
import lang24.data.type.visitor.*;

/**
 * A pointer type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemPointerType extends SemSimpleType {

	/** An object of {@code SemPointerType(null)} class. */
	public static final SemPointerType type = new SemPointerType(SemVoidType.type);

	/** The base type (or {@code null} if it denotes <code>nil</code>). */
	public final SemType baseType;

	/**
	 * Constructs a pointer type.
	 * 
	 * @param baseType The base type.
	 */
	public SemPointerType(final SemType baseType) {
		if (baseType == null)
			throw new Report.InternalError();
		this.baseType = baseType;
	}

	@Override
	public String toString() {
		return "^" + (baseType == null ? "" : baseType.toString());
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}