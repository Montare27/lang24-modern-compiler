package lang24.data.ast.tree.type;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.visitor.*;

/**
 * A record type, i.e., either a struct or union.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class AstRecType extends AstType {

	/** The components. */
	public final AstNodes<AstCmpDefn> cmps;

	/**
	 * Constructs a record type.
	 * 
	 * @param location The location.
	 * @param cmps     The components of this union.
	 */
	public AstRecType(final Location location, final AstNodes<AstCmpDefn> cmps) {
		super(location);
		this.cmps = cmps;
	}

	/**
	 * A definition of a record component.
	 * 
	 * @author bostjan.slivnik@fri.uni-lj.si
	 */
	public static class AstCmpDefn extends AstDefn {

		/**
		 * Constructs a definition of a record component.
		 * 
		 * @param location The location.
		 * @param name     The name of this record component.
		 * @param type     The type of this record component.
		 */
		public AstCmpDefn(final Locatable location, final String name, final AstType type) {
			super(location, name, type);
		}

		@Override
		public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
			return visitor.visit(this, arg);
		}

	}

}