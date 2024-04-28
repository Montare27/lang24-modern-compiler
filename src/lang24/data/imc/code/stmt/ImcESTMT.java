package lang24.data.imc.code.stmt;

import lang24.common.logger.*;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.visitor.*;

/**
 * Expression statement.
 * 
 * Evaluates expression and throws the result away.
 */
public class ImcESTMT extends ImcStmt {

	/** The expression. */
	public final ImcExpr expr;

	/**
	 * Constructs an expression statement.
	 * 
	 * @param expr The expression.
	 */
	public ImcESTMT(ImcExpr expr) {
		this.expr = expr;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction", "ESTMT");
		expr.log(logger);
		logger.endElement();
	}

	@Override
	public String toString() {
		return "ESTMT(" + expr.toString() + ")";
	}

}