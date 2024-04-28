package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * An atomic type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstAtomType extends AstType {

	/** The atomic types. */
	public enum Type {
		/** Type {@code void}. */
		VOID,
		/** Type {@code bool}. */
		BOOL,
		/** Type {@code char}. */
		CHAR,
		/** Type {@code int}. */
		INT,
	};

	/** The kind of this atomic type. */
	public final Type type;

	/**
	 * Constructs an atomic type.
	 * 
	 * @param location The location.
	 * @param type     The atomic type.
	 */
	public AstAtomType(final Locatable location, final Type type) {
		super(location);
		this.type = type;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}