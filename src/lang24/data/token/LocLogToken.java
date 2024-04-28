package lang24.data.token;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;

import lang24.common.logger.*;
import lang24.common.report.*;
import lang24.phase.lexan.*;

/**
 * A customized token that is locatable (see {@link Locatable}) and loggable
 * (see {@link Loggable}).
 *
 * @author bostjan.slivnik@fri.uni-lj.si
 */
@SuppressWarnings("serial")
public class LocLogToken extends CommonToken implements Locatable, Loggable {

	/** The location of this token. */
	private final Location location;

	/**
	 * Never used outside {@link Lang24Lexer} (see
	 * <a href="https://www.antlr.org/index.html">ANTLR</a>).
	 */
	@SuppressWarnings("doclint:missing")
	public LocLogToken(final int type, final String text) {
		super(type, text);
		setLine(0);
		setCharPositionInLine(0);
		location = new Location(getLine(), getCharPositionInLine(), getLine(),
				getCharPositionInLine() + getText().length() - 1);
	}

	/**
	 * Never used outside {@link Lang24Lexer} (see
	 * <a href="https://www.antlr.org/index.html">ANTLR</a>).
	 */
	@SuppressWarnings("doclint:missing")
	public LocLogToken(final Pair<TokenSource, CharStream> source, final int type, final int channel, final int start,
			final int stop) {
		super(source, type, channel, start, stop);
		setCharPositionInLine(getCharPositionInLine() - getText().length() + 1);
		location = new Location(getLine(), getCharPositionInLine(), getLine(),
				getCharPositionInLine() + getText().length() - 1);
	}

	@Override
	public Location location() {
		return location;
	}

	@Override
	public void log(final Logger logger) {
		if (logger == null)
			return;
		logger.begElement("token");
		if (getType() == -1) {
			logger.addAttribute("kind", "EOF");
			logger.addAttribute("lexeme", "");
		} else {
			logger.addAttribute("kind", Lang24Lexer.VOCABULARY.getSymbolicName(getType()));
			logger.addAttribute("lexeme", getText());
			location.log(logger);
		}
		logger.endElement();
	}

}
