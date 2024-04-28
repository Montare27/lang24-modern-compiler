package lang24.data.ast.tree.expr;

import lang24.common.report.*;
import lang24.data.ast.visitor.*;

/**
 * Atom expression, i.e., a constant.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstAtomExpr extends AstExpr {

	/** Types. */
	public enum Type {
		/** Constant of type {@code void}. */
		VOID,
		/** Constant of type {@code bool}. */
		BOOL,
		/** Constant of type {@code char}. */
		CHAR,
		/** Constant of type {@code int}. */
		INT,
		/** Constant of type {@code ^char}. */
		STR,
		/** Constant of a pointer type. */
		PTR,
	};

	/** The type of a constant. */
	public final Type type;

	/** The value of a constant. */
	public final String value;

	/**
	 * Constructs an atom expression, i.e., a constant.
	 * 
	 * @param location The location.
	 * @param type     The type of this constant.
	 * @param value    The value of this constant.
	 */
	public AstAtomExpr(Locatable location, Type type, String value) {
		super(location);
		this.type = type;
		this.value = value;
	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}