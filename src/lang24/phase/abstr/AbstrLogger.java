package lang24.phase.abstr;

import java.util.*;
import lang24.common.logger.*;
import lang24.common.report.Report;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;

/**
 * Abstract syntax logger.
 * 
 * This logger traverses the abstract syntax tree and produces an XML
 * description of it. Additionally, other loggers can be plugged-in to include
 * attribute values computed in subsequent phases.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class AbstrLogger implements AstVisitor<Object, String> {

	/** The logger the log should be written to. */
	private final Logger logger;

	/** A list of subvisitors for logging results of the subsequent phases. */
	private final LinkedList<AstVisitor<?, ?>> subvisitors;

	/**
	 * Construct a new visitor with a logger the log should be written to.
	 * 
	 * @param logger The logger the log should be written to.
	 */
	public AbstrLogger(final Logger logger) {
		this.logger = logger;
		this.subvisitors = new LinkedList<AstVisitor<?, ?>>();
	}

	/**
	 * Adds a new subvisitor to this visitor.
	 * 
	 * @param subvisitor The subvisitor.
	 */
	public void addSubvisitor(AstVisitor<?, ?> subvisitor) {
		subvisitors.addLast(subvisitor);
	}

	// lang24.data.ast.tree:

	@Override
	public Object visit(AstNodes<? extends AstNode> nodes, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		logger.addAttribute("id", Integer.toString(nodes.id));
		logger.addAttribute("label", nodes.getClass().getSimpleName() + "<" + elemClassName + ">");

		for (AstNode node : nodes)
			node.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			nodes.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	// lang24.data.ast.tree.defn:

	public Object visit(AstTypDefn typDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		typDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(typDefn.id));
		logger.addAttribute("label", typDefn.getClass().getSimpleName());
		logger.addAttribute("name", typDefn.name);
		typDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			typDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstVarDefn varDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		varDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(varDefn.id));
		logger.addAttribute("label", varDefn.getClass().getSimpleName());
		logger.addAttribute("name", varDefn.name);

		varDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			varDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstFunDefn funDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		funDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(funDefn.id));
		logger.addAttribute("label", funDefn.getClass().getSimpleName());
		logger.addAttribute("name", funDefn.name);
		if (funDefn.pars != null)
			funDefn.pars.accept(this, "AstParDefn");
		if (funDefn.stmt != null)
			funDefn.stmt.accept(this, null);
		if (funDefn.defns != null)
			funDefn.defns.accept(this, "AstDefn");
		funDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			funDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstFunDefn.AstRefParDefn refParDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		refParDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(refParDefn.id));
		logger.addAttribute("label", refParDefn.getClass().getSimpleName());
		logger.addAttribute("name", refParDefn.name);
		refParDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			refParDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstFunDefn.AstValParDefn valParDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		valParDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(valParDefn.id));
		logger.addAttribute("label", valParDefn.getClass().getSimpleName());
		logger.addAttribute("name", valParDefn.name);
		valParDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			valParDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	// lang24.data.ast.tree.expr:

	public Object visit(AstArrExpr arrExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		arrExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(arrExpr.id));
		logger.addAttribute("label", arrExpr.getClass().getSimpleName());
		arrExpr.arr.accept(this, null);
		arrExpr.idx.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			arrExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstAtomExpr atomExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		atomExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(atomExpr.id));
		logger.addAttribute("label", atomExpr.getClass().getSimpleName());
		logger.addAttribute("name", atomExpr.value);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			atomExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstBinExpr binExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		binExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(binExpr.id));
		logger.addAttribute("label", binExpr.getClass().getSimpleName());
		logger.addAttribute("name", binExpr.oper.name());
		binExpr.fstExpr.accept(this, null);
		binExpr.sndExpr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			binExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstCallExpr callExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		callExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(callExpr.id));
		logger.addAttribute("label", callExpr.getClass().getSimpleName());
		logger.addAttribute("name", callExpr.name);
		if (callExpr.args != null)
			callExpr.args.accept(this, "AstExpr");
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			callExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstCastExpr castExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		castExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(castExpr.id));
		logger.addAttribute("label", castExpr.getClass().getSimpleName());
		castExpr.type.accept(this, null);
		castExpr.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			castExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstCmpExpr cmpExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		cmpExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(cmpExpr.id));
		logger.addAttribute("label", cmpExpr.getClass().getSimpleName());
		logger.addAttribute("name", "." + cmpExpr.name);
		cmpExpr.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			cmpExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstNameExpr nameExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		nameExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(nameExpr.id));
		logger.addAttribute("label", nameExpr.getClass().getSimpleName());
		logger.addAttribute("name", nameExpr.name);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			nameExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstPfxExpr pfxExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		pfxExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(pfxExpr.id));
		logger.addAttribute("label", pfxExpr.getClass().getSimpleName());
		logger.addAttribute("name", pfxExpr.oper.name());
		pfxExpr.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			pfxExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstSfxExpr sfxExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		sfxExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(sfxExpr.id));
		logger.addAttribute("label", sfxExpr.getClass().getSimpleName());
		logger.addAttribute("name", sfxExpr.oper.name());
		sfxExpr.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			sfxExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstSizeofExpr sizeofExpr, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		sizeofExpr.location().log(logger);
		logger.addAttribute("id", Integer.toString(sizeofExpr.id));
		logger.addAttribute("label", sizeofExpr.getClass().getSimpleName());
		sizeofExpr.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			sizeofExpr.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	// lang24.data.ast.tree.stmt:

	public Object visit(AstAssignStmt assignStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		assignStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(assignStmt.id));
		logger.addAttribute("label", assignStmt.getClass().getSimpleName());
		assignStmt.dst.accept(this, null);
		assignStmt.src.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			assignStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstBlockStmt blockStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		blockStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(blockStmt.id));
		logger.addAttribute("label", blockStmt.getClass().getSimpleName());
		if (blockStmt.stmts != null)
			blockStmt.stmts.accept(this, "AstStmt");
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			blockStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstExprStmt exprStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		exprStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(exprStmt.id));
		logger.addAttribute("label", exprStmt.getClass().getSimpleName());
		exprStmt.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			exprStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstIfStmt ifStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		ifStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(ifStmt.id));
		logger.addAttribute("label", ifStmt.getClass().getSimpleName());
		ifStmt.cond.accept(this, null);
		ifStmt.thenStmt.accept(this, null);
		if (ifStmt.elseStmt != null)
			ifStmt.elseStmt.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			ifStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstReturnStmt retStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		retStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(retStmt.id));
		logger.addAttribute("label", retStmt.getClass().getSimpleName());
		retStmt.expr.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			retStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstWhileStmt whileStmt, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		whileStmt.location().log(logger);
		logger.addAttribute("id", Integer.toString(whileStmt.id));
		logger.addAttribute("label", whileStmt.getClass().getSimpleName());
		whileStmt.cond.accept(this, null);
		whileStmt.stmt.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			whileStmt.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	// lang24.data.ast.tree.type:

	public Object visit(AstArrType arrType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		arrType.location().log(logger);
		logger.addAttribute("id", Integer.toString(arrType.id));
		logger.addAttribute("label", arrType.getClass().getSimpleName());
		arrType.elemType.accept(this, null);
		arrType.size.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			arrType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstAtomType atomType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		atomType.location().log(logger);
		logger.addAttribute("id", Integer.toString(atomType.id));
		logger.addAttribute("label", atomType.getClass().getSimpleName());
		logger.addAttribute("name", atomType.type.name());
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			atomType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstNameType nameType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		nameType.location().log(logger);
		logger.addAttribute("id", Integer.toString(nameType.id));
		logger.addAttribute("label", nameType.getClass().getSimpleName());
		logger.addAttribute("name", nameType.name);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			nameType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstPtrType ptrType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		ptrType.location().log(logger);
		logger.addAttribute("id", Integer.toString(ptrType.id));
		logger.addAttribute("label", ptrType.getClass().getSimpleName());
		ptrType.baseType.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			ptrType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstStrType strType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		strType.location().log(logger);
		logger.addAttribute("id", Integer.toString(strType.id));
		logger.addAttribute("label", strType.getClass().getSimpleName());
		if (strType.cmps != null)
			strType.cmps.accept(this, "AstCmpDefn");
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			strType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstUniType uniType, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		uniType.location().log(logger);
		logger.addAttribute("id", Integer.toString(uniType.id));
		logger.addAttribute("label", uniType.getClass().getSimpleName());
		if (uniType.cmps != null)
			uniType.cmps.accept(this, "AstCmpDefn");
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			uniType.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

	public Object visit(AstRecType.AstCmpDefn cmpDefn, String elemClassName) {
		if (logger == null)
			return null;
		logger.begElement("node");
		cmpDefn.location().log(logger);
		logger.addAttribute("id", Integer.toString(cmpDefn.id));
		logger.addAttribute("label", cmpDefn.getClass().getSimpleName());
		logger.addAttribute("name", cmpDefn.name);
		cmpDefn.type.accept(this, null);
		for (AstVisitor<?, ?> subvisitor : subvisitors)
			cmpDefn.accept(subvisitor, null);
		logger.endElement();
		return null;
	}

}