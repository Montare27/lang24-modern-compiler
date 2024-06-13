package lang24.phase.seman;

import java.util.*;
import java.util.function.BinaryOperator;

import lang24.common.report.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;
import lang24.data.type.*;

/**
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class TypeResolver implements AstFullVisitor<SemType, Object/*** TODO OR NOT TODO ***/> {

	private List<String> _typeDefinitionsInRecursion = new ArrayList<>();


	/**
	 * Structural equivalence of types.
	 * 
	 * @param type1 The first type.
	 * @param type2 The second type.
	 * @return {@code true} if the types are structurally equivalent, {@code false}
	 *         otherwise.
	 */
	private boolean equiv(SemType type1, SemType type2) {
		return equiv(type1, type2, new HashMap<SemType, HashSet<SemType>>());
	}

	/**
	 * Structural equivalence of types.
	 * 
	 * @param type1  The first type.
	 * @param type2  The second type.
	 * @param equivs Type synonyms assumed structurally equivalent.
	 * @return {@code true} if the types are structurally equivalent, {@code false}
	 *         otherwise.
	 */
	private boolean equiv(SemType type1, SemType type2, HashMap<SemType, HashSet<SemType>> equivs) {

		if ((type1 instanceof SemNameType) && (type2 instanceof SemNameType)) {
			if (equivs == null)
				equivs = new HashMap<SemType, HashSet<SemType>>();

			if (equivs.get(type1) == null)
				equivs.put(type1, new HashSet<SemType>());
			if (equivs.get(type2) == null)
				equivs.put(type2, new HashSet<SemType>());
			if (equivs.get(type1).contains(type2) && equivs.get(type2).contains(type1))
				return true;
			else {
				HashSet<SemType> types;

				types = equivs.get(type1);
				types.add(type2);
				equivs.put(type1, types);

				types = equivs.get(type2);
				types.add(type1);
				equivs.put(type2, types);
			}
		}

		type1 = type1.actualType();
		type2 = type2.actualType();

		if (type1 instanceof SemVoidType)
			return (type2 instanceof SemVoidType);
		if (type1 instanceof SemBoolType)
			return (type2 instanceof SemBoolType);
		if (type1 instanceof SemCharType)
			return (type2 instanceof SemCharType);
		if (type1 instanceof SemIntType)
			return (type2 instanceof SemIntType);

		if (type1 instanceof SemArrayType) {
			if (!(type2 instanceof SemArrayType))
				return false;
			final SemArrayType arr1 = (SemArrayType) type1;
			final SemArrayType arr2 = (SemArrayType) type2;
			if (arr1.size != arr2.size)
				return false;
			return equiv(arr1.elemType, arr2.elemType, equivs);
		}

		if (type1 instanceof SemPointerType) {
			if (!(type2 instanceof SemPointerType))
				return false;
			final SemPointerType ptr1 = (SemPointerType) type1;
			final SemPointerType ptr2 = (SemPointerType) type2;
			if ((ptr1.baseType.actualType() instanceof SemVoidType)
					|| (ptr2.baseType.actualType() instanceof SemVoidType))
				return true;
			return equiv(ptr1.baseType, ptr2.baseType, equivs);
		}

		if (type1 instanceof SemStructType) {
			if (!(type2 instanceof SemStructType))
				return false;
			final SemStructType str1 = (SemStructType) type1;
			final SemStructType str2 = (SemStructType) type2;
			if (str1.cmpTypes.size() != str2.cmpTypes.size())
				return false;
			for (int c = 0; c < str1.cmpTypes.size(); c++)
				if (!(equiv(str1.cmpTypes.get(c), str2.cmpTypes.get(c), equivs)))
					return false;
			return true;
		}
		if (type1 instanceof SemUnionType) {
			if (!(type2 instanceof SemUnionType))
				return false;
			final SemUnionType uni1 = (SemUnionType) type1;
			final SemUnionType uni2 = (SemUnionType) type2;
			if (uni1.cmpTypes.size() != uni2.cmpTypes.size())
				return false;
			for (int c = 0; c < uni1.cmpTypes.size(); c++)
				if (!(equiv(uni1.cmpTypes.get(c), uni2.cmpTypes.get(c), equivs)))
					return false;
			return true;
		}

		throw new Report.InternalError();
	}


	//todo: to use the same as it was in NameResolver???
	@Override
	public SemType visit(AstNodes<? extends AstNode> nodes, Object arg) {
		SemTypes<SemType> semTypes = new SemTypes<>();

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

		types.forEach(t -> t.accept(this, 1));
		types.forEach(t -> t.accept(this, 2));
		vars.forEach(v -> v.accept(this, arg));
		funcs.forEach(f -> f.accept(this, 1));
		funcs.forEach(f -> f.accept(this, 2));

		return null;
	}


	@Override
	public SemType visit(AstTypDefn typDefinition, Object arg) {

		_typeDefinitionsInRecursion.add(typDefinition.name);
		typDefinition.type.accept(this, arg);
		_typeDefinitionsInRecursion.remove(typDefinition.name);


		SemType type = SemAn.isType.get(typDefinition.type);

		if(type == null) {
			if(arg == null || arg.equals(2)) {
				throw new Report.Error(typDefinition.type, "Type was not found");
			}

			return null;
		}

		return SemAn.isType.put(typDefinition, type);
	}

	private AstDefn getDefinedDefiniteOfType(AstDefn definition) {
		return SemAn.definedAt.get(definition.type);
	}

	@Override
	public SemType visit(AstVarDefn varDefn, Object arg) {
		SemType result = varDefn.type.accept(this, arg);
		//todo: check it
//		AstDefn varDefinitionType = getDefinedDefiniteOfType(varDefn);
//		if(varDefinitionType == null)
//			throw new Report.Error(varDefn, "Type from definedAt of type of VarDefn is null");
//		SemType result = SemAn.isType.get(varDefinitionType);

		if(result == null || result instanceof SemVoidType)
			throw new Report.Error(varDefn.type, "Type of Variable " + varDefn.location() + " is null or void");

		return SemAn.ofType.put(varDefn, result);
	}

	@Override
	public SemType visit(AstFunDefn funDefn, Object arg) {

		SemType returnType = null;

		if(arg != null && arg.equals(1)) {
			returnType = funDefn.type.accept(this, arg);
			SemAn.ofType.put(funDefn, returnType);

			if(returnType == null)
				throw new Report.Error(funDefn, "Return type of function is null xxx");

			if(funDefn.pars != null) {
				funDefn.pars.forEach(p -> p.accept(this, arg));
			}
		}

		if(arg != null && arg.equals(2)) {
			returnType = SemAn.ofType.get(funDefn);

			if(funDefn.defns != null) {
				funDefn.defns.accept(this, arg);
			}
			if(funDefn.stmt != null) {
				funDefn.stmt.accept(this, funDefn); //we put this arg to statements under
			}
		}

		return returnType;
	}

	@Override
	public SemType visit(AstFunDefn.AstRefParDefn refParDefn, Object arg) {
		SemType type = refParDefn.type.accept(this, arg);
		Boolean isLVal = SemAn.isLVal.get(refParDefn);

		if(isLVal == null || !isLVal)
			throw new Report.Error(refParDefn, "Called-by-reference parameter is not lval. Type: " + type);

		return SemAn.ofType.put(refParDefn, type);
	}

	@Override
	public SemType visit(AstFunDefn.AstValParDefn valParDefn, Object arg) {
		SemType type = valParDefn.type.accept(this, arg);

		if(!(type instanceof SemBoolType) && !(type instanceof SemCharType) && !(type instanceof SemIntType) && !(type instanceof SemPointerType))
			throw new Report.Error(valParDefn, "Unavailable type for val parameter! It must be an atom type! " + type);

		return SemAn.ofType.put(valParDefn, type);
	}

	@Override
	public SemType visit(AstArrExpr arrExpr, Object arg) {
		SemType arrType = arrExpr.arr.accept(this, arg);
		SemType idxType = arrExpr.idx.accept(this, arg);

		if(arrType == null || idxType == null)
			throw new Report.Error(arrExpr, "Arr or Index types are null! " + arrType + " " + idxType);

		if(!(arrType instanceof SemArrayType))
			throw new Report.Error(arrExpr, "Identifier is not array");

		if(!(idxType instanceof SemIntType))
			throw new Report.Error(arrExpr, "Index type is not Integer");

		Boolean isLval = SemAn.isLVal.get(arrExpr.arr);
		if(isLval == null || !isLval)
			throw new Report.Error(arrExpr, "Array is not LValue");

		return SemAn.ofType.put(arrExpr, ((SemArrayType) arrType).elemType);
	}

	//todo: test it. it cannot run because it's unreachable code
	@Override
	public SemType visit(AstAtomExpr atomExpr, Object arg) {

		SemType semType = switch (atomExpr.type) {
			case VOID -> SemVoidType.type;
			case BOOL -> SemBoolType.type;
			case CHAR -> SemCharType.type;
			case INT -> SemIntType.type;
			case PTR -> SemPointerType.type;
			case STR -> new SemPointerType(SemCharType.type);
		};

		SemAn.ofType.put(atomExpr, semType);
		return semType;
	}

	@Override
	public SemType visit(AstBinExpr binExpr, Object arg) {
		SemType fstType = binExpr.fstExpr.accept(this, arg);
		SemType sndType = binExpr.sndExpr.accept(this, arg);

		if(fstType == null || sndType == null)
			throw new Report.Error(binExpr,"One of types is null: " + fstType + " " + sndType);

//		todo: append this after
		boolean areEquiv = equiv(fstType, sndType);

		if(!areEquiv)
			throw new Report.Error(binExpr, "Types are not equivalent! " + fstType + " " + sndType);


		Set<AstBinExpr.Oper> intOperators = new HashSet<>(Arrays.asList(AstBinExpr.Oper.ADD, AstBinExpr.Oper.SUB, AstBinExpr.Oper.MUL, AstBinExpr.Oper.DIV, AstBinExpr.Oper.MOD));
		Set<AstBinExpr.Oper> boolOperators = new HashSet<>(Arrays.asList(AstBinExpr.Oper.AND, AstBinExpr.Oper.OR));
		Set<AstBinExpr.Oper> eqOperators = new HashSet<>(Arrays.asList(AstBinExpr.Oper.EQU, AstBinExpr.Oper.NEQ));
		Set<AstBinExpr.Oper> compOperators = new HashSet<>(Arrays.asList(AstBinExpr.Oper.LEQ, AstBinExpr.Oper.GEQ, AstBinExpr.Oper.GTH, AstBinExpr.Oper.LTH));

		//todo: finish for structural equivalence. Actual types
		if(intOperators.contains(binExpr.oper)) {

			//todo: finish with names
			if(!(fstType instanceof SemIntType) || !(sndType instanceof SemIntType))
				throw new Report.Error(binExpr, "Unable to use +/- in binary expression, where operands are not integer \n Operands types: " + fstType + " " + sndType);

			SemAn.ofType.put(binExpr, SemIntType.type);
			return SemIntType.type;
		}

		if(boolOperators.contains(binExpr.oper)) {
			if(!(fstType instanceof SemBoolType) || !(sndType instanceof SemBoolType))
				throw new Report.Error(binExpr, "Unable to use and/or in binary expression, where operands are not boolean \n Operands types: " + fstType + " " + sndType);

			SemAn.ofType.put(binExpr, SemBoolType.type);
			return SemBoolType.type;
		}

		if(eqOperators.contains(binExpr.oper)) {

			if(sndType instanceof SemPointerType && (((SemPointerType) sndType).baseType == null || ((SemPointerType) sndType).baseType == SemVoidType.type)) {
				throw new Report.Error(binExpr, "Unavailable base type for pointer type in comparison expression\nBaseType: " + ((SemPointerType) sndType).baseType);
			}

			if(!(fstType instanceof SemBoolType) && !(sndType instanceof SemCharType) && !(sndType instanceof SemIntType) && !(sndType instanceof SemPointerType))
				throw new Report.Error(binExpr, "Unable to use comparison operator in binary expression, where operands are not of the same atom type \n Operands types: " + fstType + " " + sndType);

			SemAn.ofType.put(binExpr, SemBoolType.type);
			return SemBoolType.type;
		}

		if(compOperators.contains(binExpr.oper)) {
			if(sndType instanceof SemPointerType && (((SemPointerType) sndType).baseType == null || ((SemPointerType) sndType).baseType == SemVoidType.type)) {
				throw new Report.Error(binExpr, "Unavailable base type for pointer type in equivalence expression");
			}
			if(!(fstType instanceof SemIntType) && !(sndType instanceof SemCharType) && !(sndType instanceof SemIntType) && !(sndType instanceof SemPointerType))
				throw new Report.Error(binExpr, "Unable to use equivalence operator in binary expression, where operands are not of the same atom type \n Operands types: " + fstType + " " + sndType);

			SemAn.ofType.put(binExpr, SemBoolType.type);
			return SemBoolType.type;
		}

		return null;
	}

	@Override
	public SemType visit(AstCallExpr callExpr, Object arg) {

		AstDefn definition = SemAn.definedAt.get(callExpr);

		if(!(definition instanceof AstFunDefn))
			throw new Report.Error(callExpr, "Definition is not AstFunDfn: " + definition);

		SemType resultType = SemAn.ofType.get(definition);
		if(resultType == null)
			throw new Report.Error(definition, "Result Type of function is null.");

		if (callExpr.args != null) {

			callExpr.args.forEach(a -> a.accept(this, arg));

			List<SemType> callTypes = new ArrayList<>();
			List<SemType> funTypes = new ArrayList<>();

			for(AstExpr callArg : callExpr.args)
				callTypes.add(SemAn.ofType.get(callArg));

			for(AstFunDefn.AstParDefn funArg : ((AstFunDefn) definition).pars)
				funTypes.add(SemAn.ofType.get(funArg));

			for (int i = 0; i < funTypes.size(); i++) {
				if(!equiv(callTypes.get(i), funTypes.get(i))) {
					throw new Report.Error(callExpr, "Types of arguments are not equivalent with types of function!\nIndex: " + i + " Call type: " + callTypes.get(i) + " Fun type: " + funTypes.get(i));
				}
			}
		}

		return SemAn.ofType.put(callExpr, resultType);
	}

	@Override
	public SemType visit(AstCastExpr castExpr, Object arg) {
		SemType dstType = castExpr.type.accept(this, arg);
		SemType srcType = castExpr.expr.accept(this, arg);

		if(!(dstType instanceof SemIntType) && !(dstType instanceof SemCharType) && (!(dstType instanceof SemPointerType) || ((SemPointerType) dstType).baseType == null)
				|| !(srcType instanceof SemIntType) && !(srcType instanceof SemCharType) && (!(srcType instanceof SemPointerType) || ((SemPointerType) srcType).baseType == null)) {
			throw new Report.Error("The Source or Destination type is unavailable for cast. " + srcType + " " + dstType);
		}

		return SemAn.ofType.put(castExpr, dstType);
	}

//	private AstDefn findDefinition(AstType type) {
//		AstType resultType = null;
//		if(type instanceof AstPtrType)
//			resultType = type.
//	}

	/**
	 * struct x;
	 * x.a = 32;
	 */
	@Override
	public SemType visit(AstCmpExpr cmpExpr, Object arg) {

		// 1st stage - recursively load previous CmpExpr s
		Object rootObject = cmpExpr.expr.accept(this, arg);

		AstType rootType = null;
		int ptrRecursion = 0;

		// 2. Get type of previous CmpExpr
		// if it is NameExpr -> we take a variable type
		// if it is SfxExpr or CmpExpr -> we take previous CmpExpr type
		if(cmpExpr.expr instanceof AstNameExpr nameExpr) { // it is only the 1st iteration
			AstDefn rootDefinition = SemAn.definedAt.get(nameExpr);

			if(!(rootDefinition instanceof AstVarDefn) && !(rootDefinition instanceof AstFunDefn.AstParDefn))
				throw new Report.Error(rootDefinition,
						"Unable declaration of Struct or Union. It must be variable or parameter.");

			rootType = rootDefinition.type;
		}
		else { // if we have Cmp or Sfx Expr
			AstExpr parentExpr = cmpExpr.expr;

			while(parentExpr instanceof AstSfxExpr){ //if we have SfxExpr - just extract CmpExpr{
				parentExpr = ((AstSfxExpr) parentExpr).expr;
				ptrRecursion++;
			}

			//check that it is component or nameExpr
			if(!(parentExpr instanceof AstCmpExpr) && !(parentExpr instanceof AstNameExpr))
				throw new Report.Error(parentExpr, "Sub Expression is not CmpExpr or NameExpr: " + parentExpr);

			if(SemAn.definedAt.get(parentExpr) == null ) // check that it contains in definedAt
				throw new Report.Error(parentExpr, "This expression was null ");

			rootType = SemAn.definedAt.get(parentExpr).type;
		}

		// 3. Extract parent root record type
		AstRecType rootRecordType = null;

		// 3.1 If we have PtrType: extract base type
		for(; ptrRecursion > 0; ptrRecursion--){ // ensure that we don't have PtrType
			if(!(rootType instanceof AstPtrType))
				throw new Report.Error(cmpExpr.expr, "You used too much suffix expressions. It is not PtrType, but: " + rootType);

			rootType = ((AstPtrType) rootType).baseType;
		}

		if(rootType instanceof AstNameType) {
			AstDefn typeDefinition = SemAn.definedAt.get(rootType);
			if(typeDefinition == null)
				throw new Report.Error(cmpExpr.expr, "Definition of NameType "+ rootType+" is null");

			rootRecordType = (AstRecType)typeDefinition.type;
		}
		else if(rootType instanceof AstRecType) {
			rootRecordType = (AstRecType) rootType;
		}
		else {
			throw new Report.Error(cmpExpr.expr, "Root Component is not Struct or Union, but: " + rootType);
		}

		// 4. Find cmp
		for(AstRecType.AstCmpDefn cmp : rootRecordType.cmps) {
			if (cmp.name.equals(cmpExpr.name)) {
				// 5. Put cmp into definedAt and ofType
				SemAn.definedAt.put(cmpExpr, cmp);
				SemType cmpType = SemAn.ofType.get(cmp);
				return SemAn.ofType.put(cmpExpr, cmpType);
			}
		}

		throw new Report.Error(cmpExpr, "Component "+ cmpExpr.name +" was not found");
	}

	@Override
	public SemType visit(AstNameExpr nameExpr, Object arg) {
		AstDefn definition = SemAn.definedAt.get(nameExpr);

		if(definition == null)
			throw new Report.Error(nameExpr, "Definition of NameExpr is null: " + nameExpr.name);
		SemType definitionType = SemAn.ofType.get(definition);

		if(definitionType == null)
			throw new Report.Error(nameExpr, "Definition type of NameExpr is null " + nameExpr.name + " " + definition);

		return SemAn.ofType.put(nameExpr, definitionType);
	}

	@Override
	public SemType visit(AstPfxExpr pfxExpr, Object arg) {
		pfxExpr.expr.accept(this, arg);
		SemType exprType = SemAn.ofType.get(pfxExpr.expr);

		if(pfxExpr.oper == AstPfxExpr.Oper.PTR) {
			Boolean isLval = SemAn.isLVal.get(pfxExpr.expr);

			if(isLval == null || !isLval )
				throw new Report.Error(pfxExpr, "Expression type is not LValue");

			if(exprType == null)
				throw new Report.Error(pfxExpr, "Expression type is undefined");

			return SemAn.ofType.put(pfxExpr, new SemPointerType(exprType));
		}

		if(pfxExpr.oper == AstPfxExpr.Oper.NOT)
		{
			if(!(exprType instanceof SemBoolType))
				throw new Report.Error(pfxExpr, "Unavailable operator NOT before non-bool type expression.");

			return SemAn.ofType.put(pfxExpr, SemBoolType.type);
		}

		if((pfxExpr.oper == AstPfxExpr.Oper.ADD || pfxExpr.oper == AstPfxExpr.Oper.SUB) ) {
			if(!(exprType instanceof SemIntType))
				throw new Report.Error(pfxExpr, "Unavailable operator +/- before non-int type expression.");

			return SemAn.ofType.put(pfxExpr, SemIntType.type);
		}

		return null;
	}

	@Override
	public SemType visit(AstSfxExpr sfxExpr, Object arg) {
		SemType exprType = sfxExpr.expr.accept(this, arg);
		if(!(exprType instanceof SemPointerType))
			throw new Report.Error(sfxExpr, "Base type is not Pointer Type to use Suffix ^");

		return SemAn.ofType.put(sfxExpr, ((SemPointerType) exprType).baseType);
	}

	@Override
	public SemType visit(AstSizeofExpr sizeofExpr, Object arg) {
		if(sizeofExpr.type.accept(this, arg) == null)
			throw new Report.Error(sizeofExpr.type,"Type does not exist");
		return SemAn.ofType.put(sizeofExpr, SemIntType.type);
	}

	@Override
	public SemType visit(AstAssignStmt assignStmt, Object arg) {
		assignStmt.src.accept(this, arg);
		assignStmt.dst.accept(this, arg);

		SemType srcType = SemAn.ofType.get(assignStmt.src);
		SemType dstType = SemAn.ofType.get(assignStmt.dst);

		if(srcType == null || dstType == null)
			throw new Report.Error(assignStmt, "Dst or Src type in Assign Statement is null: "+ dstType + " "+ srcType);

		if(!equiv(dstType, srcType))
			throw new Report.Error(assignStmt, "Types are not equivalent! " + dstType + " " + srcType);

		Boolean isLVal = SemAn.isLVal.get(assignStmt.dst);
		if(isLVal == null || !isLVal)
			throw new Report.Error(assignStmt.dst, "Dst expression is not LValue. Type: " + srcType);

		if(!(srcType instanceof SemBoolType)  && !(srcType instanceof SemIntType) && !(srcType instanceof SemCharType)
				&& (!(srcType instanceof SemPointerType) || ((SemPointerType) srcType).baseType == null))
			throw new Report.Error("The Destination type is unavailable for assign. " + dstType);

		return SemAn.ofType.put(assignStmt, SemVoidType.type);
	}

	@Override
	public SemType visit(AstBlockStmt blockStmt, Object arg) {

		if(blockStmt.stmts.size() == 0)
			throw new Report.Error(blockStmt, "Number of statements in BlockStatement is 0");

		for(AstStmt stmt : blockStmt.stmts) {
			stmt.accept(this, arg);
			if(SemAn.ofType.get(stmt) == null)
				throw new Report.Error(stmt, "Type of stmt of Block statement is null");
		}

		return SemAn.ofType.put(blockStmt, SemVoidType.type);
	}

	@Override
	public SemType visit(AstExprStmt exprStmt, Object arg) {
		exprStmt.expr.accept(this, arg);
		return SemAn.ofType.put(exprStmt, SemVoidType.type);
	}

	@Override
	public SemType visit(AstIfStmt ifStmt, Object arg) {
		ifStmt.cond.accept(this, arg);
		ifStmt.thenStmt.accept(this, arg);
		if (ifStmt.elseStmt != null)
			ifStmt.elseStmt.accept(this, arg);

		if(!(SemAn.ofType.get(ifStmt.cond) instanceof SemBoolType))
			throw new Report.Error(ifStmt.cond, "Type of the condition of if statement must be bool.");

		if(SemAn.ofType.get(ifStmt.thenStmt) == null)
			throw new Report.Error(ifStmt.thenStmt, "OfType of stmt is null in Then part of If statement");

		if(ifStmt.elseStmt != null && SemAn.ofType.get(ifStmt.elseStmt) == null)
			throw new Report.Error(ifStmt.elseStmt, "ElseStmt of stmt is null in Then part of If statement");

		return SemAn.ofType.put(ifStmt, SemVoidType.type);
	}

	@Override
	public SemType visit(AstReturnStmt retStmt, Object arg) {
		retStmt.expr.accept(this, arg);

		if(!(arg instanceof AstFunDefn function))
			throw new Report.Error(retStmt, "Argument is null, expected - AstFunDefn instance");

		SemType returnType = SemAn.ofType.get(retStmt.expr);
		if(returnType == null)
			throw new Report.Error(retStmt.expr, "Type of Expr of RETURN statement is null");

		SemType funcType = SemAn.isType.get(function.type);

		if(!equiv(returnType, funcType))
			throw new Report.Error(retStmt, "Return type and function type are not equivalent! " + returnType + " " + funcType);


		SemAn.returnStatements.put(retStmt, (AstFunDefn) arg);

		return SemAn.ofType.put(retStmt, SemVoidType.type);
	}

	@Override
	public SemType visit(AstWhileStmt whileStmt, Object arg) {
		whileStmt.cond.accept(this, arg);
		whileStmt.stmt.accept(this, arg);

		if(!(SemAn.ofType.get(whileStmt.cond) instanceof SemBoolType))
			throw new Report.Error(whileStmt.cond, "Type of the condition of WHILE statement must be bool.");

		if(SemAn.ofType.get(whileStmt.stmt) == null)
			throw new Report.Error(whileStmt.stmt, "Type of Stmt of WHILE is null.");

		return SemAn.ofType.put(whileStmt, SemVoidType.type);
	}

	//Done t1
	@Override
	public SemType visit(AstAtomType atomType, Object arg) {
		SemType semType = switch (atomType.type) {
			case VOID -> SemVoidType.type;
			case BOOL -> SemBoolType.type;
			case CHAR -> SemCharType.type;
			case INT -> SemIntType.type;
		};

		SemAn.isType.put(atomType, semType);
		return semType;
	}

//	private String _currentTypeDefinitionName = "";

	@Override
	public SemType visit(AstNameType nameType, Object arg) {
		AstDefn definition = SemAn.definedAt.get(nameType);

		SemType type = null;

		if(_typeDefinitionsInRecursion.contains(nameType.name)) { // if it is cyclic type
			type = new SemNameType(nameType.name);
		}
		else { // if it is not a cyclic type
			if(arg != null && arg.equals(2)) {
				definition.accept(this, arg);
			}

			if(_typeDefinitionsInRecursion.contains(nameType.name)) { // if it is cyclic type
				type = new SemNameType(nameType.name);
			}
			else {
				type = SemAn.isType.get(definition);
			}

			if(type == null && arg != null && arg.equals(2)) {
				type = new SemNameType(nameType.name);
			}
		}

		return SemAn.isType.put(nameType, type);
	}

	@Override
	public SemType visit(AstPtrType ptrType, Object arg) {
		SemType baseType = ptrType.baseType.accept(this, arg);

		if(baseType == null) {
			if(arg == null || arg.equals(2))
				throw new Report.Error(ptrType, "BaseType is null. AstType: " + ptrType.baseType);

			return null;
		}

		SemType resultType = new SemPointerType(baseType);
		return SemAn.isType.put(ptrType, resultType);
	}

	@Override
	public SemType visit(AstStrType strType, Object arg) {

		List<SemType> components = new ArrayList<>();
		if(strType.cmps.size() <= 0)
			throw new Report.Error(strType, "Struct does not contain components!");

		for(AstRecType.AstCmpDefn cmp : strType.cmps) {
			components.add(cmp.accept(this, arg));
		}

		SemStructType resultType = new SemStructType(components);
		SemAn.isType.put(strType, resultType);
		return resultType;
	}

	@Override
	public SemType visit(AstUniType uniType, Object arg) {
		List<SemType> components = new ArrayList<>();

		if(uniType.cmps.size() <= 0)
			throw new Report.Error(uniType, "Union does not contain components!");

		for(AstRecType.AstCmpDefn cmp : uniType.cmps) {
			components.add(visit(cmp, arg));
		}

		SemUnionType resultType = new SemUnionType(components);
		SemAn.isType.put(uniType, resultType);
		return resultType;
	}


	//todo: after
	@Override
	public SemType visit(AstRecType.AstCmpDefn cmpDefn, Object arg) {
		SemType resultType = cmpDefn.type.accept(this, arg);

//		Report.info("CmpDefn: " + resultType + " " + arg);
		if((resultType == null || resultType instanceof SemVoidType) && (arg == null || arg.equals(2))) {
			throw new Report.Error(cmpDefn, "Result type of component is void or null: " + arg);
		}

		//todo: wait before adding the line udner
		SemAn.ofType.put(cmpDefn, resultType);

		return resultType;
	}


	@Override
	public SemType visit(AstArrType arrType, Object arg) {

		SemType semType = arrType.elemType.accept(this, arg);

		if((semType == null || semType instanceof SemVoidType) && (arg == null || arg.equals(2)))
			throw new Report.Error(arrType, "Element Type was not found for Array or it's Void! Root type: " + arrType.elemType);

		//Suppose that there can be only int const
		SemArrayType result = new SemArrayType(semType, getSizeValue(arrType));

        return SemAn.isType.put(arrType, result);
	}

	private static long getSizeValue(AstArrType arrType) {
		long sizeValue = 0;
		try {
			AstAtomExpr size = (AstAtomExpr) arrType.size;

			if(size.type != AstAtomExpr.Type.INT)
				throw new Report.Error(arrType.size, "Size of array is not INT");

			sizeValue = Long.parseLong(size.value);
		} catch (ClassCastException | NumberFormatException ex) {
			throw new Report.Error(arrType.size, "Size of array is not atom type or unable to convert to int");
		}

		if(sizeValue <= 0 || sizeValue > Math.pow(2, 63) - 1)
			throw new Report.Error(arrType.size, "Size of an array is out of range!");
		return sizeValue;
	}
}