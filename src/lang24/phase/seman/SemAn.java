package lang24.phase.seman;

import lang24.data.ast.attribute.*;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.stmt.AstReturnStmt;
import lang24.data.type.*;
import lang24.phase.*;

import java.util.List;

/**
 * Semantic analysis phase.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemAn extends Phase {

	/** Mapping of names to definitions. */
	public static final Attribute<AstNode, AstDefn> definedAt = new Attribute<AstNode, AstDefn>();

	/**
	 * Attribute specifying whether something is an lvalue or not (where applicable).
	 */
	public static final Attribute<AstNode, Boolean> isLVal = new Attribute<AstNode, Boolean>();

	/**
	 * Attribute specifying what type is defined by a particular language construct
	 * (where applicable).
	 */
	public static final Attribute<AstNode, SemType> isType = new Attribute<AstNode, SemType>();

	/**
	 * Attribute specifying what is a type of a particular language construct (where
	 * applicable).
	 */
	public static final Attribute<AstNode, SemType> ofType = new Attribute<AstNode, SemType>();

	/**
	 * Attribute dictionary between Return Statements and Function Definitions
	 */
	public static final Attribute<AstReturnStmt, AstFunDefn> returnStatements = new Attribute<>();

	/**
	 * Phase construction.
	 */
	public SemAn() {
		super("seman");
	}

}