JAVA            = java --enable-preview
ANTLRDIR        = ../lib/antlr-4.13.1-complete.jar
XSLTPROC	= xsltproc
RM              = 'rm' -fr

TARGETPHASE	= all
LOGGEDPHASE	= all

.PHONY	: %
%	: %.lang24
	$(RM) -fr $@.*.xml $@.*.html
	if $(JAVA) -cp ../bin:../src:$(ANTLRDIR) lang24.Compiler --xsl=../lib/xsl/ --logged-phase=$(LOGGEDPHASE) --target-phase=$(TARGETPHASE) $< ; then for XML in `'ls' -1 $@-*.xml` ; do $(XSLTPROC) $$XML -o `basename $$XML xml`html; done ; fi

.PHONY  : clean
clean   :
	$(RM) *~
	$(RM) *xml *html

