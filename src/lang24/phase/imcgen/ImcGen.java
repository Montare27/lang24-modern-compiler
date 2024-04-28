package lang24.phase.imcgen;

import lang24.data.ast.attribute.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.mem.*;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.code.stmt.*;
import lang24.phase.*;

/**
 * Intermediate code generation phase.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class ImcGen extends Phase {

	/**
	 * The entry point of the body.
	 * */
	public static final Attribute<AstFunDefn, MemLabel> entryLabel = new Attribute<AstFunDefn, MemLabel>();

	/**
	 * The end of the function
	 * The entry point of the epilogue.
	 * */
	public static final Attribute<AstFunDefn, MemLabel> exitLabel = new Attribute<AstFunDefn, MemLabel>();

	/**
	 * Maps statements to intermediate code.
	 * */
	public static final Attribute<AstStmt, ImcStmt> stmtImc = new Attribute<AstStmt, ImcStmt>();

	/**
	 *  Maps expressions to intermediate code.
	 *  */
	public static final Attribute<AstExpr, ImcExpr> exprImc = new Attribute<AstExpr, ImcExpr>();

	/**
	 * Phase construction.
	 */
	public ImcGen() {
		super("imcgen");
	}

}