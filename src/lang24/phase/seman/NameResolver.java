package lang24.phase.seman;

import lang24.common.report.Locatable;
import lang24.common.report.Report;
import lang24.data.ast.tree.AstNode;
import lang24.data.ast.tree.AstNodes;
import lang24.data.ast.tree.defn.AstDefn;
import lang24.data.ast.tree.defn.AstFunDefn;
import lang24.data.ast.tree.defn.AstTypDefn;
import lang24.data.ast.tree.defn.AstVarDefn;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.AstFullVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Name resolver.
 * The name resolver connects each node of an abstract syntax tree where a name
 * is used with the node where it is defined. The only exceptions are struct and
 * union component names which are connected with their definitions by type
 * resolver. The results of the name resolver are stored in
 * {@link lang24.phase.seman.SemAn#definedAt}.
 */
public class NameResolver implements AstFullVisitor<Object, Object> {

	/** Constructs a new name resolver. */
	public NameResolver() {}

	/** The symbol table. */
	private final SymbTable symbTable = new SymbTable();

	private void insertDefn(AstDefn node) {
		try{
			symbTable.ins(node.name, node);
		}
		catch (SymbTable.CannotInsNameException e) {
			throw new Report.Error(node, "Was thrown CannotInsNameException with name: '" + node.name + "' node: " + node);
		}
	}

	/**
	 * Finds existed definition by na
	 * Throws an exception if
	 * @param locatable
	 * @param name
	 * @return
	 */
	private AstDefn findDefn(Locatable locatable, String name) {
		try{
			return symbTable.fnd(name);
		}
		catch (SymbTable.CannotFndNameException | ClassCastException e) {
			throw new Report.Error(locatable,  "Definition '" + name + "' is not defined");
		}
	}


	/*Type definition and Nodes */

	//Done
	@Override
	public Object visit(AstNodes<? extends AstNode> nodes, Object arg) { // add precedence we just visit all the nodes

//		symbTable.newScope();
		if(arg != null && arg.equals("ToList")) {
			List<AstNode> list = new ArrayList<>();
			for (AstNode n : nodes) list.add(n);
			return list;
		}

		List<AstTypDefn> types = new ArrayList<>();
		List<AstVarDefn> vars = new ArrayList<>();
		List<AstFunDefn> funcs = new ArrayList<>();

		for(var node : nodes) {
			if(node instanceof AstTypDefn)
				types.add((AstTypDefn) node);
			if(node instanceof AstVarDefn)
				vars.add((AstVarDefn) node);
			if(node instanceof AstFunDefn)
				funcs.add((AstFunDefn) node);
		}


		// the point is that we add here precedence.
		// precedence 1 means that we gonna observe only left-hand part of defn
		// precedence 2 observes the right-hand part of defn
		// for out task we need to interact only with the precedence 1.
		types.forEach(t -> t.accept(this, 1));
		types.forEach(t -> t.accept(this, 2));
		vars.forEach(v -> v.accept(this, arg));
		funcs.forEach(f -> f.accept(this, 1));
		funcs.forEach(f -> f.accept(this, 2));

		return null;
	}

	@Override
	public Object visit(AstVarDefn varDefn, Object arg) {
		insertDefn(varDefn);
		varDefn.type.accept(this, arg);
		return null;
	}

	@Override
	public Object visit(AstTypDefn typDefn, Object arg) { // we just pass to the symtable
		if((int)arg == 1) {  // precedence 1 - observe left-hand side of the defintion

			insertDefn(typDefn);
		}
		else if((int)arg == 2) {  // precedence 1 - observe right-hand side of the defintion
			typDefn.type.accept(this, arg);
		}
		return null;
	}

	/*Function*/

	@Override
	@SuppressWarnings("unchecked")
	public Object visit(AstFunDefn funDefn, Object arg) {

		List<AstFunDefn.AstParDefn> pars = funDefn.pars != null
			? (List<AstFunDefn.AstParDefn>)funDefn.pars.accept(this, "ToList")
			: new ArrayList<>();

		// First precedence. Add to SymTable name, type and parameter types
		if(arg != null && (int)arg == 1) {
			insertDefn(funDefn);
			funDefn.type.accept(this, arg);
			pars.forEach(p -> p.accept(this, "type")); // add parameter types
		}

		// Second precedence. Add tp SymTable parameter names, definitions and statements
		if(arg != null && (int)arg == 2) {
			symbTable.newScope();
			pars.forEach(p -> p.accept(this, "name")); // add name types

				symbTable.newScope();

				if(funDefn.defns != null)
					funDefn.defns.accept(this, null);

				if(funDefn.stmt != null)
					funDefn.stmt.accept(this, null);

				symbTable.oldScope();

			symbTable.oldScope();
		}

		return null;
	}

	@Override
	public Object visit(AstFunDefn.AstRefParDefn refParDefn, Object arg) {
		return visitPar(refParDefn, arg);
	}

	@Override
	public Object visit(AstFunDefn.AstValParDefn valParDefn, Object arg) {
		return visitPar(valParDefn, arg);
	}

	private Object visitPar(AstFunDefn.AstParDefn parDefn, Object arg) {
		if(arg != null && arg.equals("name"))
			insertDefn(parDefn);
		if(arg != null && arg.equals("type"))
			parDefn.type.accept(this, null);
		return null;
	}

	@Override
	public Object visit(AstBinExpr binExpr, Object arg) {
		binExpr.fstExpr.accept(this, null);
		binExpr.sndExpr.accept(this, null);
		return null;
	}

	@Override
	public Object visit(AstNameType nameType, Object arg) {
		AstDefn defn = findDefn(nameType, nameType.name);

		if(!(defn instanceof AstTypDefn))
			throw new Report.Error(nameType, "Founded name is not a type");

		SemAn.definedAt.put(nameType, defn);
		return null;
	}

	@Override
	public Object visit(AstNameExpr nameExpr, Object arg) {
		AstDefn defn = findDefn(nameExpr, nameExpr.name);

		if(!(defn instanceof AstVarDefn) && !(defn instanceof AstFunDefn.AstParDefn))
			throw new Report.Error(nameExpr, "Variable " + nameExpr.name + " was not found!");

		SemAn.definedAt.put(nameExpr, defn);
		return nameExpr;
	}


	@Override
	public Object visit(AstCallExpr callExpr, Object arg) {
		AstDefn defn = findDefn(callExpr,callExpr.name);

		if(!(defn instanceof AstFunDefn funDefn))
			throw new Report.Error(callExpr, "There is no AstFunDefn: " + callExpr.name);

        if(callExpr.args != null)
			callExpr.args.forEach(a -> a.accept(this, arg));

		if(funDefn.pars == null && callExpr.args != null)
			throw new Report.Error(callExpr, "Call expression contains arguments, while function does not");

		if(funDefn.pars != null && callExpr.args == null)
			throw new Report.Error(callExpr, "Call expression does not contain arguments, while function does");

		if(funDefn.pars != null && funDefn.pars.size() != callExpr.args.size())
			throw new Report.Error(callExpr, "Amount of call expression's arguments does not equal with amount of parameters of function: " + funDefn.pars.size() + " " + callExpr.args.size());

		SemAn.definedAt.put(callExpr, funDefn);

        return null;
	}

	@Override
	public Object visit(AstPfxExpr pfxExpr, Object arg) {
		return pfxExpr.expr.accept(this, null);
	}

	@Override
	public Object visit(AstSfxExpr sfxExpr, Object arg) {
		return sfxExpr.expr.accept(this, null);
	}

	@Override
	public Object visit(AstSizeofExpr sizeofExpr, Object arg) {
		return sizeofExpr.type.accept(this, arg);
	}


	/*Statements*/


	@Override
	public Object visit(AstAssignStmt assignStmt, Object arg) {
		assignStmt.dst.accept(this, arg);
		return assignStmt.src.accept(this, arg);
	}


	@Override
	public Object visit(AstBlockStmt blockStmt, Object arg) {
		symbTable.newScope();
			blockStmt.stmts.forEach(s -> s.accept(this, arg));
		symbTable.oldScope();

		return null;
	}


	@Override
	public Object visit(AstExprStmt exprStmt, Object arg) {
		return exprStmt.expr.accept(this, arg);
	}


	@Override
	public Object visit(AstIfStmt ifStmt, Object arg) {
		symbTable.newScope();
			ifStmt.cond.accept(this, arg);
			ifStmt.thenStmt.accept(this, arg);
		symbTable.oldScope();

		if(ifStmt.elseStmt != null) {
			symbTable.newScope();
				ifStmt.elseStmt.accept(this, arg);
			symbTable.oldScope();
		}

		return null;
	}


	@Override
	public Object visit(AstReturnStmt retStmt, Object arg) {
		return retStmt.expr.accept(this, null);
	}


	@Override
	public Object visit(AstWhileStmt whileStmt, Object arg) {
		symbTable.newScope();

			whileStmt.cond.accept(this, arg);
			symbTable.newScope();
				whileStmt.stmt.accept(this, arg);
			symbTable.oldScope();

		symbTable.oldScope();
		return null;
	}


	@Override
	public Object visit(AstArrType arrType, Object arg) {
		return arrType.elemType.accept(this, arg);
	}

	@Override
	public Object visit(AstPtrType ptrType, Object arg) {
		return ptrType.baseType.accept(this, arg);
	}


	@Override
	public Object visit(AstStrType strType, Object arg) {
		checkOnDuplicates(strType);
		strType.cmps.forEach(c -> c.accept(this, arg));
		return null;
	}

	@Override
	public Object visit(AstUniType uniType, Object arg) {
		checkOnDuplicates(uniType);
		uniType.cmps.forEach(c -> c.accept(this, arg));
		return null;
	}

	private void checkOnDuplicates(AstRecType recType) {

		List<AstRecType.AstCmpDefn> cmps = new ArrayList<>();

		for(AstRecType.AstCmpDefn cmp: recType.cmps)
			cmps.add(cmp);

		for(AstRecType.AstCmpDefn cmp : cmps)
			if(cmps.stream().filter(c -> c.name.equals(cmp.name)).toList().size() > 1)
				throw new Report.Error(cmp, "Duplicate of components: " + cmp.name);
	}

	public Object visit(AstRecType.AstCmpDefn cmpDefn, Object arg) {
		return cmpDefn.type.accept(this, null);
	}
}