package lang24.phase.synan;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import lang24.common.report.*;
import lang24.data.token.*;
import lang24.phase.*;
import lang24.phase.lexan.*;

/**
 * Syntax analysis phase.
 */
public class SynAn extends Phase {

	/** The parse tree. */
	public static lang24.phase.synan.Lang24Parser.SourceContext tree;

	/** The ANTLR parser that actually performs syntax analysis. */
	public final lang24.phase.synan.Lang24Parser parser;

	/**
	 * Phase construction: sets up logging and the ANTLR lexer and parser.
	 *
	 * @param lexan The lexical analyzer.
	 */
	public SynAn(final LexAn lexan) {
		super("synan");
		parser = new Lang24Parser(new CommonTokenStream(lexan.lexer));
		parser.removeErrorListeners();
		parser.addErrorListener(new BaseErrorListener() {
			public void syntaxError(final Recognizer<?, ?> recognizer, final Object offendingSymbol, final int line,
					final int charPositionInLine, final String msg, final RecognitionException e) {
				throw new Report.Error(new Location(line, charPositionInLine),
						"Unexpected symbol '" + ((LocLogToken) offendingSymbol).getText() + "'.");
			}
		});
	}

	/**
	 * Logs a parse tree.
	 *
	 * @param tree The parse tree to be logged.
	 */
	public void log(final ParseTree tree) {
		if (logger == null)
			return;
//		System.out.println("Log enter");

		if (tree instanceof TerminalNodeImpl) {
			final TerminalNodeImpl node = (TerminalNodeImpl) tree;
			((LocLogToken) (node.getPayload())).log(logger);
		}
		if (tree instanceof ParserRuleContext) {
			final ParserRuleContext node = (ParserRuleContext) tree;
			logger.begElement("node");
			logger.addAttribute("label", Lang24Parser.ruleNames[node.getRuleIndex()]);
			final int numChildren = node.getChildCount();
			for (int i = 0; i < numChildren; i++)
				log(node.getChild(i));

			logger.endElement();
		}
	}

}
