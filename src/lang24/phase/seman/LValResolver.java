package lang24.phase.seman;

import lang24.common.report.Report;
import lang24.data.ast.tree.defn.AstDefn;
import lang24.data.ast.tree.defn.AstFunDefn;
import lang24.data.ast.tree.defn.AstVarDefn;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.visitor.AstFullVisitor;

/**
 * Lvalue resolver.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class LValResolver implements AstFullVisitor<Boolean, Object> {

	/** Constructs a new lvalue resolver. */
	public LValResolver() {
	}


	@Override
	public Boolean visit(AstVarDefn varDefn, Object arg) {

		return SemAn.isLVal.put(varDefn, true);
	}


	@Override
	public Boolean visit(AstFunDefn.AstRefParDefn refParDefn, Object arg) {
		return visitPar(refParDefn, arg);
	}


	@Override
	public Boolean visit(AstFunDefn.AstValParDefn valParDefn, Object arg) {
		return visitPar(valParDefn, arg);
	}

	private Boolean visitPar(AstFunDefn.AstParDefn par, Object arg) {
		par.type.accept(this, arg);
		return SemAn.isLVal.put(par, true);
	}


	/*Uses*/
	@Override
	public Boolean visit(AstNameExpr nameExpr, Object arg) {
		AstDefn definition = SemAn.definedAt.get(nameExpr);

		if(!(definition instanceof AstVarDefn) && !(definition instanceof AstFunDefn.AstParDefn)) {
			return false;
		}

		return SemAn.isLVal.put(nameExpr, true);
	}


	@Override
	public Boolean visit(AstSfxExpr sfxExpr, Object arg) {
		AstDefn definition = null;

		Boolean isLVal = sfxExpr.expr.accept(this, arg);
		if(isLVal == null || !isLVal) {
			return false;
		}

		return SemAn.isLVal.put(sfxExpr, true);
	}


	@Override
	public Boolean visit(AstArrExpr arrExpr, Object arg) {
		arrExpr.idx.accept(this, arg);

		if(arrExpr.arr.accept(this, arg) != null) {
			return SemAn.isLVal.put(arrExpr, true);
		}
		return null;
	}


	@Override
	public Boolean visit(AstCmpExpr cmpExpr, Object arg) {
		cmpExpr.expr.accept(this, arg);

		if(SemAn.isLVal.get(cmpExpr.expr) == null)
			return false;

		return SemAn.isLVal.put(cmpExpr, true);
	}


	@Override
	public Boolean visit(AstCastExpr castExpr, Object arg) {
		castExpr.type.accept(this, arg);
		castExpr.expr.accept(this, arg);

		if(SemAn.isLVal.get(castExpr.expr) == null)
			return false;

		return SemAn.isLVal.put(castExpr, true);
	}
}
