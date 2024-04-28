package lang24.phase.abstr;

import lang24.data.ast.tree.*;
import lang24.phase.*;

/**
 * Abstract syntax phase.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class Abstr extends Phase {

	/** The abstract syntax tree. */
	public static AstNode tree;

	/**
	 * Phase construction.
	 */
	public Abstr() {
		super("abstr");
	}

}