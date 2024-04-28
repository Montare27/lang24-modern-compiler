JAVA		= java
ANTLR		= org.antlr.v4.Tool
ANTLRDIR	= ../../../../lib/antlr-4.13.1-complete.jar
RM		= 'rm' -fr

lexer	: Lang24Lexer.java Lang24Lexer.interp Lang24Lexer.tokens
Lang24Lexer.java Lang24Lexer.interp Lang24Lexer.tokens : Lang24Lexer.g4
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) $<
	mv Lang24Lexer.java Lang24Lexer.java-orig
	sed 's/@SuppressWarnings({"all"/@SuppressWarnings({"doclint:missing", "all"/' < Lang24Lexer.java-orig > Lang24Lexer.java
	$(RM) ../../../../bin/lang24/phase/lexan/Lang24Lexer.class

.PHONY	: clean
clean	:
	$(RM) *~
	$(RM) Lang24*.java* *interp *tokens
