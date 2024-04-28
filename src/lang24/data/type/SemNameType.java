package lang24.data.type;

import java.util.*;
import lang24.common.report.*;
import lang24.data.type.visitor.*;

/**
 * A type name.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemNameType extends SemType {

	/** The name. */
	public final String name;

	/** Type this named type represents. */
	private SemType type = null;

	/**
	 * Constructs a new type name.
	 * 
	 * @param name The name.
	 */
	public SemNameType(final String name) {
		this.name = name;
	}

	/**
	 * Defines the type this named type represents.
	 * 
	 * @param type The type this named type represents.
	 */
	public void define(final SemType type) {
		if (this.type != null)
			throw new Report.InternalError();
		this.type = type;
	}

	/**
	 * Returns the type this named type represents.
	 * 
	 * @return The type this named type represents.
	 */
	public SemType type() {
		if (this.type == null)
			throw new Report.InternalError();
		return type;
	}

	@Override
	public SemType actualType() {
		final HashSet<SemNameType> types = new HashSet<SemNameType>();
		types.add(this);
		return this.type.actualType(types);
	}

	@Override
	public SemType actualType(final HashSet<SemNameType> types) {
		if (types.contains(this))
			throw new Report.InternalError();
		types.add(this);
		return this.type.actualType(types);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}