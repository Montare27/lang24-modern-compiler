package lang24.data.ast.visitor;

import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;

/**
 * An abstract syntax tree visitor that does nothing.
 * 
 * @param <Result>   The result type.
 * @param <Argument> The argument type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public interface AstNullVisitor<Result, Argument> extends AstVisitor<Result, Argument> {

	// lang24.data.ast.tree:

	@Override
	public default Result visit(AstNodes<? extends AstNode> nodes, Argument arg) {
		return null;
	}

	// lang24.data.ast.tree.defn:

	@Override
	public default Result visit(AstTypDefn typDefn, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstVarDefn varDefn, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstFunDefn funDefn, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstFunDefn.AstRefParDefn refParDefn, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstFunDefn.AstValParDefn valParDefn, Argument arg) {
		return null;
	}

	// lang24.data.ast.tree.expr:

	@Override
	public default Result visit(AstArrExpr arrExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstAtomExpr atomExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstBinExpr binExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstCallExpr callExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstCastExpr castExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstCmpExpr cmpExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstNameExpr nameExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstPfxExpr pfxExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstSfxExpr sfxExpr, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstSizeofExpr sizeofExpr, Argument arg) {
		return null;
	}

	// lang24.data.ast.tree.stmt:

	@Override
	public default Result visit(AstAssignStmt assignStmt, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstBlockStmt blockStmt, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstExprStmt callStmt, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstIfStmt ifStmt, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstReturnStmt retStmt, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstWhileStmt whileStmt, Argument arg) {
		return null;
	}

	// lang24.data.ast.tree.type:

	@Override
	public default Result visit(AstArrType arrType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstAtomType atomType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstNameType nameType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstPtrType ptrType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstStrType strType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstUniType uniType, Argument arg) {
		return null;
	}

	@Override
	public default Result visit(AstRecType.AstCmpDefn cmpDefn, Argument arg) {
		return null;
	}

}