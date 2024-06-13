JAVA		= java --enable-preview
JAVAC		= javac --enable-preview --release 21 -encoding UTF8
ANTLR           = org.antlr.v4.Tool
ANTLRDIR	= lib/antlr-4.13.1-complete.jar
RM		= 'rm' -fr
FIND		= 'find'
SUBMISSION_DIR = ../../submissions
UNI_ID = 70092585

NAME = lexan
ZIP = false
OUT = out

all	:
	if [ -d src/lang24/phase/lexan ] ; then $(MAKE) -C src/lang24/phase/lexan ; fi
	if [ -d src/lang24/phase/synan ] ; then $(MAKE) -C src/lang24/phase/synan ; fi

	# Gradle modification start
	# Copy module-inof.java to module-info.java-orig
	mv src/module-info.java src/module-info.java-orig
	# Change require statement
	sed 's/requires org\.antlr\.antlr4\.runtime;/requires antlr;/' < src/module-info.java-orig > src/module-info.java

	# Original compilation command
	$(JAVAC) --module-path $(ANTLRDIR) --source-path src -d bin src/lang24/Compiler.java

	# Move file back to original name
	mv src/module-info.java-orig src/module-info.java
	# Gradle modification end

	@echo ":-) OK"

.PHONY	: clean

run:
	$(MAKE) clean
	$(MAKE) all
	$(MAKE) run2

run3:
	$(MAKE) run2
	$(MAKE) execute

run2:
	if [ -d prg ] ; then $(MAKE) -C prg bubblesort TARGETPHASE=regall K=4; fi


# "-x" to avoid error "-b 65536" - buffer size
execute:
	@./mmixal -x -b 65536 prg/$(OUT).mms
	@./mmix prg/$(OUT).mmo
	#clear && ./mmixal -x -b 256 prg/$(OUT).mms && ./mmix prg/$(OUT).mmo


deploy:
	$(MAKE) clean
	if [ -d $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)] ; then rm -r $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME) ; fi
	rm -r $(SUBMISSION_DIR)/$(UNI_ID)-*
	cp -r . $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)
	rm -r $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)/*gradle*
	rm -r $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)/.[a-z]*
	rm -r $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)/build
	rm -r $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)/lib/*.jar
	cd $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)
	zip -r $(UNI_ID)-$(NAME).zip $(SUBMISSION_DIR)/$(UNI_ID)-$(NAME)/*
	echo ":-) Solution was deployed"

clean	:
	if [ -d doc ] ; then $(MAKE) -C doc clean ; fi
	if [ -d src ] ; then $(MAKE) -C prg clean ; fi
	if [ -d src/lang24/phase/lexan ] ; then $(MAKE) -C src/lang24/phase/lexan clean ; fi
	if [ -d src/lang24/phase/synan ] ; then $(MAKE) -C src/lang24/phase/synan clean ; fi
	if [ -d src/lang24/phase/*/.antlr ] ; then rm -r src/lang24/phase/*/.antlr ; fi
	if [ -d src/gen ] ;then rm -r src/gen ;fi
	$(FIND) . -type f -iname "*~" -exec $(RM) {} \;
	$(FIND) . -type f -iname "*.class" -exec $(RM) {} \;
	$(RM) bin
