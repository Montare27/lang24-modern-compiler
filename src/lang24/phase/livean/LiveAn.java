package lang24.phase.livean;

import lang24.common.report.Report;
import lang24.data.mem.*;
import lang24.data.asm.*;
import lang24.phase.*;
import lang24.phase.asmgen.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

/**
 * Liveness analysis.
 */
public class LiveAn extends Phase {

	private boolean changeDetector = false;

	public LiveAn() {
		super("livean");
	}

	public void analysis() {
		for(Code code : AsmGen.codes) {
			Report.info("Instr size " + code.frame.label.name + "=" + code.instrs.size());
			processInstructions(code.instrs);
		}
	}

	private void processInstructions(Vector<AsmInstr> instructions) {
		changeDetector = true;
		int iterationNumber = 1;

		while(changeDetector) {

			changeDetector = false;
			AsmInstr prevInstr = null;

			for (AsmInstr instr : instructions) {

				if (iterationNumber == 1 && !instr.in().containsAll(instr.uses()))  // first step fill in() with uses(), runs only once
					addInTemps(instr, new HashSet<>(new ArrayList<>(instr.uses())));  // in_i = uses+i

				if (prevInstr != null && !prevInstr.out().containsAll(instr.in()))  // out_{i-1} = in_i
					addOutTemps(prevInstr, instr.in());

				HashSet<MemTemp> subSet = generateSubsetOfOutAndDef(instr);  // in_i = out_i / def_i

				if (!instr.in().containsAll(subSet))
					addInTemps(instr, subSet);

				prevInstr = instr;
			}

			iterationNumber++;
		}
	}

	private HashSet<MemTemp> generateSubsetOfOutAndDef(AsmInstr instr) {
		HashSet<MemTemp> out = (HashSet<MemTemp>) instr.out().clone();
		instr.defs().forEach(out::remove);
		return out;
	}

	private void addInTemps(AsmInstr destination, HashSet<MemTemp> collection) {
		destination.addInTemps(collection);
		changeDetector = true;
	}

	private void addOutTemps(AsmInstr destination, HashSet<MemTemp> collection) {
		destination.addOutTemp(collection);
		changeDetector = true;
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
				logger.begElement("temps");
				logger.addAttribute("name", "use");
				for (MemTemp temp : instr.uses()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "def");
				for (MemTemp temp : instr.defs()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "in");
				for (MemTemp temp : instr.in()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "out");
				for (MemTemp temp : instr.out()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.endElement();
			}
			logger.endElement();
			logger.endElement();
		}
	}

}
