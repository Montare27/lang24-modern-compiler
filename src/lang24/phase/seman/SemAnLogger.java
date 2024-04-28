package lang24.phase.seman;

import lang24.common.logger.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;
import lang24.data.type.*;
import lang24.data.type.visitor.*;

/**
 * Semantic analysis logger.
 * 
 * This logger prints out the XML description of semantic attributes attached to
 * the abstract syntax tree. It does not traverse the entire abstract syntax
 * tree. Instead, it is used as a {@link lang24.phase.abstr.AbstrLogger}
 * plug-in.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemAnLogger implements AstNullVisitor<Object, Object>, SemVisitor<Object, Boolean> {

	/** The logger the log should be written to. */
	private final Logger logger;

	/**
	 * Construct a new visitor with a logger the log should be written to.
	 * 
	 * @param logger The logger the log should be written to.
	 */
	public SemAnLogger(final Logger logger) {
		this.logger = logger;
	}

	/**
	 * Logs all attributes of a node.
	 * 
	 * @param node The node.
	 */
	private void logAttributes(final AstNode node) {
		{
			final AstDefn defn = SemAn.definedAt.get(node);
			if (defn != null) {
				logger.begElement("definedat");
				logger.addAttribute("id", Integer.toString(defn.id()));
				logger.addAttribute("location", defn.location().toString());
				logger.endElement();
			}
		}
		{
			final SemType type = SemAn.isType.get(node);
			if (type != null) {
				logger.begElement("istype");
				type.accept(this, node instanceof AstTypDefn);
				logger.endElement();
			}
		}
		{
			final SemType type = SemAn.ofType.get(node);
			if (type != null) {
				logger.begElement("oftype");
				type.accept(this, node instanceof AstTypDefn);
				logger.endElement();
			}
		}
		{
			final Boolean lval = SemAn.isLVal.get(node);
			if ((lval != null) && (lval == true)) {
				logger.begElement("lvalue");
				logger.endElement();
			}
		}
	}

	// lang24.data.ast.tree:

	@Override
	public Object visit(AstNodes<? extends AstNode> nodes, Object arg) {
		return null;
	}

	// lang24.data.ast.tree.defn:

	@Override
	public Object visit(AstTypDefn typeDefn, Object arg) {
		logAttributes(typeDefn);
		return null;
	}

	@Override
	public Object visit(AstVarDefn varDefn, Object arg) {
		logAttributes(varDefn);
		return null;
	}

	@Override
	public Object visit(AstFunDefn funcDefn, Object arg) {
		logAttributes(funcDefn);
		return null;
	}

	@Override
	public Object visit(AstFunDefn.AstRefParDefn refParDefn, Object arg) {
		logAttributes(refParDefn);
		return null;
	}

	@Override
	public Object visit(AstFunDefn.AstValParDefn valParDefn, Object arg) {
		logAttributes(valParDefn);
		return null;
	}

	// lang24.data.ast.tree.expr:

	@Override
	public Object visit(AstArrExpr arrExpr, Object arg) {
		logAttributes(arrExpr);
		return null;
	}

	@Override
	public Object visit(AstAtomExpr atomExpr, Object arg) {
		logAttributes(atomExpr);
		return null;
	}

	@Override
	public Object visit(AstBinExpr binExpr, Object arg) {
		logAttributes(binExpr);
		return null;
	}

	@Override
	public Object visit(AstCallExpr callExpr, Object arg) {
		logAttributes(callExpr);
		return null;
	}

	@Override
	public Object visit(AstCastExpr castExpr, Object arg) {
		logAttributes(castExpr);
		return null;
	}

	@Override
	public Object visit(AstCmpExpr compExpr, Object arg) {
		logAttributes(compExpr);
		return null;
	}

	@Override
	public Object visit(AstNameExpr nameExpr, Object arg) {
		logAttributes(nameExpr);
		return null;
	}

	@Override
	public Object visit(AstPfxExpr pfxExpr, Object arg) {
		logAttributes(pfxExpr);
		return null;
	}

	@Override
	public Object visit(AstSfxExpr sfxExpr, Object arg) {
		logAttributes(sfxExpr);
		return null;
	}

	@Override
	public Object visit(AstSizeofExpr sizeofExpr, Object arg) {
		logAttributes(sizeofExpr);
		return null;
	}

	// lang24.data.ast.tree.stmt:

	@Override
	public Object visit(AstAssignStmt assignStmt, Object arg) {
		logAttributes(assignStmt);
		return null;
	}

	@Override
	public Object visit(AstBlockStmt blockStmt, Object arg) {
		logAttributes(blockStmt);
		return null;
	}

	@Override
	public Object visit(AstExprStmt callStmt, Object arg) {
		logAttributes(callStmt);
		return null;
	}

	@Override
	public Object visit(AstIfStmt ifStmt, Object arg) {
		logAttributes(ifStmt);
		return null;
	}

	@Override
	public Object visit(AstReturnStmt retStmt, Object arg) {
		logAttributes(retStmt);
		return null;
	}

	@Override
	public Object visit(AstWhileStmt whileStmt, Object arg) {
		logAttributes(whileStmt);
		return null;
	}

	// lang24.data.ast.tree.type:

	@Override
	public Object visit(AstArrType arrType, Object arg) {
		logAttributes(arrType);
		return null;
	}

	@Override
	public Object visit(AstAtomType atomType, Object arg) {
		logAttributes(atomType);
		return null;
	}

	@Override
	public Object visit(AstNameType nameType, Object arg) {
		logAttributes(nameType);
		return null;
	}

	@Override
	public Object visit(AstPtrType ptrType, Object arg) {
		logAttributes(ptrType);
		return null;
	}

	@Override
	public Object visit(AstStrType strType, Object arg) {
		logAttributes(strType);
		return null;
	}

	@Override
	public Object visit(AstUniType uniType, Object arg) {
		logAttributes(uniType);
		return null;
	}

	@Override
	public Object visit(AstRecType.AstCmpDefn compDefn, Object arg) {
		logAttributes(compDefn);
		return null;
	}

	// lang24.data.type:

	@Override
	public Object visit(SemVoidType voidType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(voidType.id));
		logger.addAttribute("label", voidType.getClass().getSimpleName());
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemBoolType boolType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(boolType.id));
		logger.addAttribute("label", boolType.getClass().getSimpleName());
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemCharType charType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(charType.id));
		logger.addAttribute("label", charType.getClass().getSimpleName());
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemIntType intType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(intType.id));
		logger.addAttribute("label", intType.getClass().getSimpleName());
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemArrayType arrType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(arrType.id));
		logger.addAttribute("label", arrType.getClass().getSimpleName() + "[" + arrType.size + "]");
		arrType.elemType.accept(this, false);
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemPointerType ptrType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(ptrType.id));
		logger.addAttribute("label", ptrType.getClass().getSimpleName());
		if (ptrType.baseType != null)
			ptrType.baseType.accept(this, false);
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemStructType strType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(strType.id));
		logger.addAttribute("label", strType.getClass().getSimpleName());
		for (final SemType cmpType : strType.cmpTypes)
			cmpType.accept(this, false);
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemUnionType uniType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(uniType.id));
		logger.addAttribute("label", uniType.getClass().getSimpleName());
		for (final SemType cmpType : uniType.cmpTypes)
			cmpType.accept(this, false);
		logger.endElement();
		return null;
	}

	@Override
	public Object visit(SemNameType nameType, Boolean full) {
		logger.begElement("type");
		logger.addAttribute("id", Integer.toString(nameType.id));
		logger.addAttribute("label", nameType.getClass().getSimpleName());
		logger.addAttribute("name", nameType.name);
		if (full == true)
			nameType.type().accept(this, false);
		logger.endElement();
		return null;
	}

}