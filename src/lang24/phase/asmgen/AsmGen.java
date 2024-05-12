package lang24.phase.asmgen;

import java.util.*;
import java.util.stream.Collectors;

import lang24.common.report.Report;
import lang24.data.imc.code.stmt.ImcStmt;
import lang24.data.lin.*;
import lang24.data.asm.*;
import lang24.data.mem.MemTemp;
import lang24.phase.*;
import lang24.phase.imclin.*;

/**
 * Machine code generator.
 */
public class AsmGen extends Phase {

	public static MemTemp FP;

	public static Vector<Code> codes = new Vector<Code>();  // the final target is to fill this table

	private static final Vector<AsmInstr> instructionsPerFunction = new Vector<>();  // temporary target is to fill this field

	public AsmGen() {
		super("asmgen");
	}

	public static void addInstruction(AsmInstr instr){
//		logAddedInstruction(instr);
		instructionsPerFunction.add(instr);
	}

	private Code replaceWithFp(Code code) {

		MemTemp FP = code.frame.FP;  // $253
//		MemTemp SP = code.frame.SP;  // $254 todo: where should we pass it?

		for(AsmInstr instr : code.instrs) {
			if(instr instanceof AsmOPER oper) {
				if(oper.instr().contains(FP.toString())) {
					String instrString = oper.instr().replace(FP.toString(), "$253");
					oper = new AsmOPER(instrString, oper.uses(), oper.defs(), oper.jumps());
				}
			}
		}

		return code;
	}

	private static void logAddedInstruction(AsmInstr instr) {
		Report.info("Add instruction: " + instr.toString());
		Report.info("With defns: " + instr.defs().stream().map(MemTemp::toString).collect(Collectors.joining(", ")));
		Report.info("With uses: " + instr.uses().stream().map(MemTemp::toString).collect(Collectors.joining(", ")));
	}

	public static void addSomeInstructions(List<AsmInstr> instrs){
		instrs.forEach(AsmGen::addInstruction);
	}

	private final CodeGenerator codeGenerator = new CodeGenerator();

	public void genAsmCodes() {
		for (LinCodeChunk codeChunk : ImcLin.codeChunks()) {
			FP = codeChunk.frame.FP;
			Code code = processLinCodeChunkIntoCode(codeChunk);
			codes.add(replaceWithFp(code));
		}

	}

	/**
	 * processes each Code fragment
	 */
	private Code processLinCodeChunkIntoCode(LinCodeChunk linCodeChunk) {
		CodeGenerator.FP = linCodeChunk.frame.FP;

		processStatements(linCodeChunk.stmts());  // processes each statement

		Code code = new Code(linCodeChunk.frame,
				linCodeChunk.entryLabel,
				linCodeChunk.exitLabel,
				instructionsPerFunction);  // creates new instance of code

		instructionsPerFunction.clear();  // clean vector of instructions per this functions
		return code;  // return the result
	}

	private void processStatements(Vector<ImcStmt> stmts) {
		stmts.forEach(s -> codeGenerator.visit(s, null));
	}

	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			logger.begElement("code");
			logger.addAttribute("prologue", code.entryLabel.name);
			logger.addAttribute("body", code.entryLabel.name);
			logger.addAttribute("epilogue", code.exitLabel.name);
			logger.addAttribute("tempsize", Long.toString(code.tempSize));
			code.frame.log(logger);
			logger.begElement("instructions");
			for (AsmInstr instr : code.instrs) {
				logger.begElement("instruction");
				logger.addAttribute("code", instr.toString());
				logger.endElement();
			}
			logger.endElement();
			logger.endElement();
		}
	}

}
