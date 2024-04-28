JAVA		= java
ANTLR		= org.antlr.v4.Tool
ANTLRDIR	= ../../../../lib/antlr-4.13.1-complete.jar
RM		= 'rm' -fr

parser	: Lang24Parser.java Lang24Parser.interp Lang24Parser.tokens
Lang24Parser.java Lang24Parser.interp Lang24Parser.tokens : Lang24Parser.g4 ../lexan/Lang24Lexer.java ../lexan/Lang24Lexer.interp ../lexan/Lang24Lexer.tokens
	$(JAVA) -cp $(ANTLRDIR):$(CLASSPATH) $(ANTLR) -no-listener -no-visitor -lib ../lexan $<
	mv Lang24Parser.java Lang24Parser.java-orig
	sed 's/@SuppressWarnings({"all"/@SuppressWarnings({"doclint:missing", "all"/' < Lang24Parser.java-orig > Lang24Parser.java
	$(RM) ../../../../bin/lang24/phase/synan/Lang24Parser.class

../lexan/Lang24Lexer.java ../lexan/Lang24Lexer.interp ../lexan/Lang24Lexer.tokens :
	$(MAKE) -C ../lexan

.PHONY	: clean
clean	:
	$(RM) *~
	$(RM) Lang24*.java *interp *tokens
