package lang24.phase.lexan;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import lang24.common.report.*;
import lang24.data.token.*;
import lang24.phase.*;

/**
 * Lexical analysis phase.
 */
public class LexAn extends Phase {

	/** The ANTLR lexer that actually performs lexical analysis. */
	public final Lang24Lexer lexer;

	/**
	 * Phase construction: sets up logging and the ANTLR lexer.
	 */
	public LexAn() {
		super("lexan");

		final String srcFileName = lang24.Compiler.cmdLineOptValue("--src-file-name");
		try {
			lexer = new Lang24Lexer(CharStreams.fromFileName(srcFileName));
			lexer.setTokenFactory(new LocLogTokenFactory());
		} catch (IOException __) {
			throw new Report.Error("Cannot open file '" + srcFileName + "'.");
		}
	}

	/**
	 * A customized token factory which logs tokens.
	 */
	private class LocLogTokenFactory implements TokenFactory<LocLogToken> {

		/**
		 * Constructs a new token factory.
		 */
		private LocLogTokenFactory() {
			super();
		}

		@Override
		public LocLogToken create(int type, String text) {
			LocLogToken token = new LocLogToken(type, text);
			token.log(logger);
			return token;
		}

		@Override
		public LocLogToken create(Pair<TokenSource, CharStream> source, int type, String text, int channel, int start,
				int stop, int line, int charPositionInLine) {
			LocLogToken token = new LocLogToken(source, type, channel, start, stop);
			token.log(logger);
			return token;
		}
	}

}