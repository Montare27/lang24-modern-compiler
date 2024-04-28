package lang24.data.ast.tree.defn;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * A definition of a function.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AstFunDefn extends AstDefn {

	/** The parameters. */
	public final AstNodes<AstParDefn> pars;

	/** The code. */
	public final AstStmt stmt;

	/** The local definitions in this function. */
	public final AstNodes<AstDefn> defns;

	/**
	 * Constructs a definition of a function.
	 * 
	 * @param location The location.
	 * @param name     The name of this function.
	 * @param pars     The parameters.
	 * @param type     The result type of this function.
	 * @param stmt     The code of this function.
	 * @param defns    The local definitions in this function.
	 */
	public AstFunDefn(final Locatable location, final String name, final AstNodes<AstParDefn> pars, final AstType type,
			final AstStmt stmt, final AstNodes<AstDefn> defns) {
		super(location, name, type);
		this.pars = pars;
		this.stmt = stmt;
		this.defns = defns;
	}

	/**
	 * A parameter definition.
	 * 
	 * @author bostjan.slivnik@fri.uni-lj.si
	 */
	public static abstract class AstParDefn extends AstDefn {

		/**
		 * Constructs a parameter definition.
		 * 
		 * @param location The location.
		 * @param name     The name of this parameter.
		 * @param type     The type of this parameter.
		 */
		public AstParDefn(final Locatable location, final String name, final AstType type) {
			super(location, name, type);
		}

	}

	/**
	 * A definition of a call-by-reference parameter .
	 * 
	 * @author bostjan.slivnik@fri.uni-lj.si
	 */
	public static class AstRefParDefn extends AstParDefn {

		/**
		 * Constructs a definition of a call-by-reference parameter.
		 * 
		 * @param location The location.
		 * @param name     The name of the this call-by-reference parameter.
		 * @param type     The type of the this call-by-reference parameter.
		 */
		public AstRefParDefn(final Locatable location, final String name, final AstType type) {
			super(location, name, type);
		}

		@Override
		public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
			return visitor.visit(this, arg);
		}

	}

	/**
	 * A definition of a call-by-value parameter.
	 * 
	 * @author bostjan.slivnik@fri.uni-lj.si
	 */
	public static class AstValParDefn extends AstParDefn {

		/**
		 * Constructs a definition of a call-by-value parameter.
		 * 
		 * @param location The location.
		 * @param name     The name of the this call-by-value parameter.
		 * @param type     The type of the this call-by-value parameter.
		 */
		public AstValParDefn(final Locatable location, final String name, final AstType type) {
			super(location, name, type);
		}

		@Override
		public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
			return visitor.visit(this, arg);
		}

	}

	@Override
	public <Result, Argument> Result accept(AstVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}