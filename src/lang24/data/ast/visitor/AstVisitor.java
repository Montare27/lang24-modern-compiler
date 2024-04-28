package lang24.data.ast.visitor;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;

/**
 * An abstract syntax tree visitor.
 * 
 * @param <Result>   The result type.
 * @param <Argument> The argument type.
 *
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public interface AstVisitor<Result, Argument> {

	// lang24.data.ast.tree:

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstNodes<? extends AstNode> nodes, Argument arg) {
		throw new Report.InternalError();
	}

	// lang24.data.ast.tree.defn:

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstTypDefn typDefn, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstVarDefn varDefn, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstFunDefn funDefn, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstFunDefn.AstRefParDefn refParDefn, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstFunDefn.AstValParDefn valParDefn, Argument arg) {
		throw new Report.InternalError();
	}

	// lang24.data.ast.tree.expr:

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstArrExpr arrExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstAtomExpr atomExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstBinExpr binExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstCallExpr callExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstCastExpr castExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstCmpExpr cmpExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstNameExpr nameExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstPfxExpr pfxExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstSfxExpr sfxExpr, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstSizeofExpr sizeofExpr, Argument arg) {
		throw new Report.InternalError();
	}

	// lang24.data.ast.tree.stmt:

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstAssignStmt assignStmt, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstBlockStmt blockStmt, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstExprStmt exprStmt, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstIfStmt ifStmt, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstReturnStmt retStmt, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstWhileStmt whileStmt, Argument arg) {
		throw new Report.InternalError();
	}

	// lang24.data.ast.tree.type:

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstArrType arrType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstAtomType atomType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstNameType nameType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstPtrType ptrType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstStrType strType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstUniType uniType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(AstRecType.AstCmpDefn cmpDefn, Argument arg) {
		throw new Report.InternalError();
	}

}