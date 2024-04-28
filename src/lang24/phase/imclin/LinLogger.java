package lang24.phase.imclin;

import lang24.common.logger.*;
import lang24.data.ast.visitor.*;
import lang24.data.imc.code.stmt.*;
import lang24.data.lin.*;

public class LinLogger implements AstNullVisitor<Object, String> {

	/** The logger the log should be written to. */
	private final Logger logger;

	/**
	 * Constructs a new visitor with a logger the log should be written to.
	 * 
	 * @param logger The logger the log should be written to.
	 */
	public LinLogger(Logger logger) {
		this.logger = logger;
	}

	// *** CHUNK LOGGER ***

	public void log(LinDataChunk dataChunk) {
		if (logger == null)
			return;
		logger.begElement("datachunk");
		logger.addAttribute("label", dataChunk.label.name);
		logger.addAttribute("size", Long.toString(dataChunk.size));
		logger.addAttribute("init", dataChunk.init);
		logger.endElement();
	}

	public void log(LinCodeChunk codeChunk) {
		if (logger == null)
			return;
		logger.begElement("codechunk");
		logger.addAttribute("prologuelabel", codeChunk.frame.label.name);
		logger.addAttribute("bodylabel", codeChunk.entryLabel.name);
		logger.addAttribute("epiloguelabel", codeChunk.exitLabel.name);
		codeChunk.frame.log(logger);
		for (ImcStmt stmt : codeChunk.stmts()) {
			logger.begElement("stmt");
			stmt.log(logger);
			logger.endElement();
		}
		logger.endElement();
	}

}