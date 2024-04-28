package lang24.data.imc.code.stmt;

import lang24.common.logger.*;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.visitor.*;

/**
 * Move operation.
 * 
 * Evaluates the destination, evaluates the source, and moves the source to the
 * destination. If the root node of the destination is {@link ImcMEM}, then the
 * source is stored to the memory address denoted by the subtree of that
 * {@link ImcMEM} node. If the root node of the destination is {@link ImcTEMP},
 * the source is stored in the temporary variable.
 */
public class ImcMOVE extends ImcStmt {

	/** The destination. */
	public final ImcExpr dst;

	/** The source. */
	public final ImcExpr src;

	/** Constructs a move operation. */
	public ImcMOVE(ImcExpr dst, ImcExpr src) {
		this.dst = dst;
		this.src = src;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction", "MOVE");
		dst.log(logger);
		src.log(logger);
		logger.endElement();
	}

	@Override
	public String toString() {
		return "MOVE(" + dst.toString() + "," + src.toString() + ")";
	}

}