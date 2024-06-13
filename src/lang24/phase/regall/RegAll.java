package lang24.phase.regall;

import lang24.common.report.Report;
import lang24.data.asm.AsmInstr;
import lang24.data.asm.AsmMOVE;
import lang24.data.asm.AsmOPER;
import lang24.data.asm.Code;
import lang24.data.mem.MemTemp;
import lang24.phase.Phase;
import lang24.phase.asmgen.AsmGen;
import lang24.phase.asmgen.CodeGenerator;
import lang24.phase.asmgen.InstrArgument;
import lang24.phase.asmgen.Instructions;
import lang24.phase.livean.LiveAn;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static lang24.phase.asmgen.CodeGenerator.generateAsmOper;

/**
 * Register allocation.
 */
public class RegAll extends Phase {

	public static int K = 4;
	private final LiveAn liveAn = new LiveAn();
	public RegAll() {
		super("regall");
	}

	/**
	 * Fills initial collection of non-precolored and not processed temps
	 */
	private void generateInitial() {
		for(var code : AsmGen.codes) {

			HashSet<MemTemp> tempsPerCode = new HashSet<>();

			for(var instr : code.instrs) {
				tempsPerCode.addAll(instr.defs());
				tempsPerCode.addAll(instr.uses());
			}

			functionToTempsMap.add(new Tuple<>(code, tempsPerCode));
			initial.addAll(tempsPerCode);
		}

		Report.info("Generating initials has ended: " + initial.size());
	}

	/** Mapping of temporary variables to registers. */
	public static final HashMap<MemTemp, Integer> tempToReg = new HashMap<MemTemp, Integer>();

	private final List<MemTemp> preColored = new ArrayList<>();  // machine registers
	private final HashSet<MemTemp> initial = new HashSet<>();  // temp registers, not pre colored nor processed
	private final List<MemTemp> simplifyWorklist = new ArrayList<>(); // list of low-degree non MR nodes
	private final List<MemTemp> freezeWorklist = new ArrayList<>();  // low-degree MR nodes
	private final List<MemTemp> spilledNodes = new ArrayList<>();  // high-degree nodes
	private final List<MemTemp> spillWorklist = new ArrayList<>();  // nodes marked to spill during this round
	private final List<MemTemp> coalescedNodes = new ArrayList<>();  // registers that have been coalesced
	private final List<MemTemp> coloredNodes = new ArrayList<>();  // nodes successfully colored
	private final Stack<MemTemp> selectStack = new Stack<>();  // removed temp vars from the stack


	/*Moves*/
	private final Set<AsmMOVE> coalescedMoves = new HashSet<AsmMOVE>();  // moves that have been coalesced
	private final Set<AsmMOVE> constrainedMoves = new HashSet<AsmMOVE>();  // moves whose src and dst interfere
	private final Set<AsmMOVE> frozenMoves = new HashSet<AsmMOVE>();  // moves that will no longer be considered for coalescing
	private final Set<AsmMOVE> worklistMoves = new HashSet<AsmMOVE>();  // moves enable for positive coalescing
	private final Set<AsmMOVE> activeMoves = new HashSet<AsmMOVE>();  // moves not yet ready for coalescing

	/*Other*/
	private final List<Tuple<MemTemp, MemTemp>> adjSet = new ArrayList<>();  // DON'T USE DIRECTLY. set of edges (u, v), if (u,v) is in adjSet then (v,u) is in the set
	private final HashMap<MemTemp, HashSet<MemTemp>> adjList = new HashMap<>();  // adjacency list of the graph for each non precolored u. adjList[u] - a set of nodes that interfere with u
	private final HashMap<MemTemp, Integer> degree = new HashMap<>();  // array that contains degree for each node
	private final HashMap<MemTemp, HashSet<AsmMOVE>> moveList = new HashMap<>();  // mapping from a node to the list of moves it is associated with
	private final HashMap<MemTemp, MemTemp> alias = new HashMap<>();  // when a move (u, v) has been coalesced, and v put in coalescedNodes, then alias(v) = u.

	/* Mapping between the function and mem temps */
	private final List<Tuple<Code, HashSet<MemTemp>>> functionToTempsMap = new ArrayList<>();

	/* Functions for access to AdjSet */
	private void addToAdjSet(MemTemp u, MemTemp v) {
		adjSet.add(new Tuple<>(u, v));
		adjSet.add(new Tuple<>(v, u));
	}

	private Predicate<Tuple<MemTemp, MemTemp>> containsEdgePredicate(MemTemp u, MemTemp v) {
		return a -> (a.value1.equals(u) && a.value2.equals(v)) ||
					(a.value1.equals(v) && a.value2.equals(u));
	}

	private void removeFromAdjSet(MemTemp u, MemTemp v) {
		adjSet.removeIf(containsEdgePredicate(u, v));
	}

	private boolean containsInAdjSet(MemTemp u, MemTemp v) {
		return adjSet.stream()
				.anyMatch(containsEdgePredicate(u, v));
	}

	private static int i = 0;

	// RegAll functions
	public void allocate() {
		liveAn.analysis();  // refill code chunks
		generateInitial();  // init lists with temps
		build();  // build interference graph
		makeWorklist();  // fill list to simplify and spill

		if(i == 2) return;

		do {
			if(!simplifyWorklist.isEmpty()) simplify();
			if(!worklistMoves.isEmpty()) coalesce();
			if(!freezeWorklist.isEmpty()) freeze();
//			if(!spillWorklist.isEmpty()) selectSpill();
			Report.info("SS=" + simplifyWorklist.size() + " MOVE=" +worklistMoves.size() + " FR=" + freezeWorklist.size() + " SP=" + spillWorklist.size());
		} while(!simplifyWorklist.isEmpty() || !worklistMoves.isEmpty() ||
				!freezeWorklist.isEmpty() );

		assignColors();  // select stage, map colors with temps
		if(!spillWorklist.isEmpty()) {  // if we have nodes we want to spill
			rewriteProgram();  // spill of actual spills
			Report.info("Iteration end");
			i++;
			allocate();  // repeat
		}

		Report.info("RegAll end");
		removeMoves();
	}

	/**
	 * Build interference graph
	 */
	private void build() {
		for(Code block  : AsmGen.codes) {
//			functionToTempsMap.add(new Tuple<>(block, block.instrs));

			HashSet<MemTemp> live = getLiveOut(block);  // get temps which survived in block


//			Report.info("Instr/block=" + block.instrs.size() + ", livedOut=" + live.stream().map(MemTemp::toString).collect(Collectors.joining(", ")));

			for(AsmInstr instr : block.instrs.reversed()) {
				// move-related
				if(instr instanceof AsmMOVE move) {
					move.uses().forEach(live::remove);
					for (var n : union(move.defs(), move.uses())) {
						HashSet<AsmMOVE> list = moveList.get(n);
						if(list == null) list = new HashSet<>();
						list.add(move);
						moveList.put(n, list);
					}
					worklistMoves.add(move);
				}

				// make edges between defs of the instruction instr
				// and livedOut instructions: other definitions and livedOut instructions
				// and previous uses

				live.addAll(instr.defs());
				for (var d : instr.defs()) {
					for(var l : live) {
						addEdge(l, d);  // so live and d are edges
					}
				}

				instr.defs().forEach(live::remove);
				live.addAll(instr.uses());
			}
		}

		assignEmptyNodes();
		Report.info("Build. adj=" + adjList.size() + " K=" + K + " MOVE=" + worklistMoves.size());
//		printGraph(adjList.keySet());
	}


	// Generate pre-defined nodes we work with
	private void makeWorklist() {

//		Report.

		for(var n : initial) {
			Integer degreeValue = degree.get(n);
			if(degreeValue == null) degreeValue = 0;

			if(degreeValue >= K) {
				spillWorklist.add(n);
			}
			else if (isMoveRelated(n)) {
				freezeWorklist.add(n);
			}
			else {
				simplifyWorklist.add(n);
			}
		}

//		Report.info("Spills: ");
//		printGraph(spillWorklist);

		Report.info("Spill Worklist: " + spillWorklist.stream().map(MemTemp::toString).collect(Collectors.joining(",")));
//
//		Report.info("Simplify Worklist: " + simplifyWorklist.stream().map(MemTemp::toString).collect(Collectors.joining(",")));
//
//		Report.info("Freeze Worklist: " + freezeWorklist.stream().map(MemTemp::toString).collect(Collectors.joining(",")));

		initial.clear();
		Report.info("Worklist. SP=" + spillWorklist.size() + " SS=" + simplifyWorklist.size() + " FR=" + freezeWorklist.size());
	}




	// fills selectStack
	private void simplify() {
		List<MemTemp> simplifyWorklistCopy = new ArrayList<>();

        // simplifyWorklist can be filled after decrement degree
        while (!simplifyWorklist.isEmpty()) {
            simplifyWorklistCopy.addAll(simplifyWorklist);
            simplifyWorklist.clear();

            for (var n : simplifyWorklistCopy) {
                if (!selectStack.contains(n))
                    selectStack.push(n);   // stack in which we add from graph, color it and build the graph again

				// remove the element by decrementing a degree of all the members
                for (var m : adjacent(n))
                    decrementDegree(m);
            }

            simplifyWorklistCopy.clear();
        }

		Report.info("Simplify. SS=" + selectStack.size() + " SP=" + spillWorklist.size() + " FR=" + freezeWorklist.size());
	}

	private void coalesce() {
		for(var m : worklistMoves) {

			// m = copy(x, y) we assume that x - use and y - destination
			MemTemp y = getAlias(m.defs().getFirst());
			MemTemp x = getAlias(m.uses().getFirst());
			MemTemp u = null;
			MemTemp v = null;

			if(preColored.contains(y)) {
				u = y;
				v = x;
			}
			else {
				u = x;
				v = y;
			}

			List<MemTemp> vAdjacent = adjacent(v);
			boolean okForAll = true;
			for(var t : vAdjacent) {
				if(!ok(t, u)) {
					okForAll = false;
				}
			}

			if(u.equals(v)) {
//				Report.info("Coalesce. u=v");
				coalescedMoves.add(m);
				addWorklist(u);
			}
			else if(preColored.contains(v) || containsInAdjSet(u, v)) {
//				Report.info("Coalesce. in adj set");
				constrainedMoves.add(m);
				addWorklist(u);
				addWorklist(v);
			}
			else if ((preColored.contains(u) && okForAll) || (!preColored.contains(u) && conservative(union(adjacent(u), vAdjacent)))) {
//				Report.info("Coalesce. other stuff");
				coalescedMoves.add(m);
				combine(u, v);
				addWorklist(u);
			}
			else {
//				Report.info("Coalesce. else");
				activeMoves.add(m);
			}
		}

		worklistMoves.clear();
		Report.info("Coalesce. CoalM="+coalescedMoves.size() + " ConstrM=" + constrainedMoves.size() + " SS=" + simplifyWorklist.size() + " FR="+ freezeWorklist.size());
	}

	private void freeze() {

		List<MemTemp> freezeWorklistCopy = new ArrayList<>();
		while(!freezeWorklist.isEmpty()) {
			freezeWorklistCopy.addAll(freezeWorklist);

			for(var u : freezeWorklistCopy) {
				simplifyWorklist.add(u);
				freezeWorklist.remove(u);
				freezeMoves(u);
			}

			freezeWorklistCopy.clear();
		}




//		freezeWorklist.clear();
		Report.info("Freeze: SS=" + simplifyWorklist.size() + " FM=" + frozenMoves.size() + " FR" + freezeWorklist.size());
	}


	private void selectSpill() {


//		int previousSize = spilledNodes.size();
//		Report.info("Select. SP=" + spilledNodes.size());
//		Report.info("Test coloring");
//
//		// we reassign colors and try to separate spills into non-spills and actual spills
//		Stack<MemTemp> selectStackCopy = new Stack<>();
//		selectStackCopy.addAll(selectStack);
//		selectStackCopy.addAll(spilledNodes);
////		selectStack.addAll(simplifyWorklist);
////		assignColors();
////		selectStack.addAll(selectStackCopy);
////
//
//		spilledNodes.clear();
//
//
//
//		HashMap<MemTemp, Integer> localTempToReg = new HashMap<>();
//		List<MemTemp> localColoredNodes = new ArrayList<>();
//
//
//		while(!selectStackCopy.isEmpty()) {
//			MemTemp n = selectStackCopy.pop();
//			List<Integer> okColors = getRange(0, K-1);  // list of all possible colors
//
//			// Compute coalescing part. If alias (merged with node) is in colored or precolored
//			// -> remove its color from okColors
//			for(MemTemp w : adjList.get(n)) {
//				MemTemp alias = getAlias(w);
//				if (union(localColoredNodes, preColored).contains(alias)) {
//					okColors.remove(localTempToReg.get(alias));
//				}
//			}
//
//			// if okColors is empty ->
//			if(okColors.isEmpty()) {
//				Report.info("Coloring. Spill=" + n);
//				spillWorklist.add(n);
//			}
//			else {
//				localColoredNodes.add(n);
//
//				// todo: fix that
//				localTempToReg.put(n, okColors.getFirst());
//			}
//		}
//
//		// coalesced nodes must have the same color
//		for(var n : coalescedNodes) {
//			localTempToReg.put(n, localTempToReg.get(getAlias(n)));
//		}

//		Report.info("Select. Stat: Prev=" + previousSize + " New=" + spillWorklist.size());



//		// out problem is: we have worklist of potential spills
//		// we need to separate spills from that list in: actual and non-spill nodes
//		// we need to rebuild graph but with assigned colors already
//		Report.info("Colored nodes: " + coloredNodes.size());
//		for(var m : spillWorklist) {
//
//
//			if(coloredNodes.contains(m)) {
//				Report.info("Colored node " + m + " was colored!!!");
//				simplifyWorklist.add(m);
//				selectStack.pop()
//				freezeMoves(m);
//		}
//		spillWorklist.clear();
		//
//			//todo: use heuristics?
//
//		}

//		spillWorklist.clear();
	}

	/**
	 * select stage, map colors with temps
	 */
	private void assignColors() {
		Report.info("Coloring. Stack=" + selectStack.size() + " CoalN=" + coloredNodes.size() + " SP_before=" + spillWorklist.size());
		while(!selectStack.isEmpty()) {
			MemTemp n = selectStack.pop();
			List<Integer> okColors = getRange(0, K-1);  // list of all possible colors

			// Compute coalescing part. If alias (merged with node) is in colored or precolored
			// -> remove its color from okColors
			for(MemTemp w : adjList.get(n)) {
				MemTemp alias = getAlias(w);
				if (union(coloredNodes, preColored).contains(alias)) {
					okColors.remove(tempToReg.get(alias));
				}
			}

			// if okColors is empty ->
			if(okColors.isEmpty()) {
				Report.info("Coloring. Spill=" + n);
				spillWorklist.add(n);
			}
			else {
				coloredNodes.add(n);

				// todo: fix that
				tempToReg.put(n, okColors.getFirst());
			}
		}

		// coalesced nodes must have the same color
		for(var n : coalescedNodes) {
			tempToReg.put(n, tempToReg.get(getAlias(n)));
		}

		Report.info("Coloring: Colored=" + coloredNodes.size() + " SP=" + spillWorklist.size() + " SpillN=" + spillWorklist.size());
	}


	/**
	 * If AssignColors spills, then RewriteProgram allocates memory locations
	 * for the spilled temporaries and inserts store and fetch instructions to
	 * access them.
	 */
	private void rewriteProgram(){

		// allocate memory for each v in spilledNodes
		// create a new temp v for each definition and each use,
		// in the program (instructions), insert a store after each
		// definition of v, a fetch before use of a v
		// put all the v into a set newTemps

		//todo: figure out the selection to actual spills



		// the code below spills all the nodes.
		for(MemTemp spillTemp : spillWorklist){
			if(spilledNodes.contains(spillTemp)) {
				Report.info("In spilled: " + spillTemp);
				continue;
			}

			Code code = findFunctionByTemp(spillTemp);  // get function where this temp appear
			if(code == null) {  // if function is null -> exception
				throw new Report.Error("Temp was not found in functions: " + spillTemp);
			}

			spilledNodes.add(spillTemp);
			code.tempSize += 8;  // increase tempSize

			// create a copy of code.instr for iterating Avoid ConcurrentModifierException
			List<AsmInstr> newInstructionList = new ArrayList<>(code.instrs);

			long offset = - code.frame.locsSize - 8 /*SL size*/ - code.tempSize;  // future offset of new temp
			for(AsmInstr instr : newInstructionList) {  // go through all the instructions
				// we need to find its appearance in uses or defs
				if(!instr.uses().contains(spillTemp) && !instr.defs().contains(spillTemp))
					continue;

				MemTemp offsetTemp = new MemTemp();  // future temp for offset

				// instructions for offset
				List<AsmInstr> newOffsetInstructions = CodeGenerator.generateNumberInstructions(offsetTemp, offset);
					MemTemp replaceTemp = new MemTemp();  // temp to replace spilled temp
				int index = code.instrs.indexOf(instr);  // as the appearance in defs or uses is assured - get its index

				String newInstrString = "";
				List<MemTemp> defs = new ArrayList<>(2);
				List<MemTemp> uses = new ArrayList<>(2);
				if(instr instanceof AsmOPER oper) {
					newInstrString = oper.instr().replace(spillTemp.toString(), replaceTemp.toString());

					defs.addAll(oper.defs());
					uses.addAll(oper.uses());

//					Report.info("Instr: " + oper.toString() + " T=" + spillTemp + " R=" +replaceTemp);//
//					Report.info("Oper with d: " + oper.defs().stream().map(MemTemp::toString).collect(Collectors.joining(",")) + " s: " + oper.uses().stream().map(MemTemp::toString).collect(Collectors.joining(",")));

					if(uses.contains(spillTemp)) {
//						Report.info("Replace use");
						int id = uses.indexOf(spillTemp);
						uses.remove(spillTemp);
						if(id == 0) uses.addFirst(replaceTemp);
						else uses.add(replaceTemp);
					}

					if(defs.contains(spillTemp)) {
//						Report.info("Replace def");
						int id = defs.indexOf(spillTemp);
						defs.remove(spillTemp);
						if(id == 0) defs.addFirst(replaceTemp);
						else defs.add(replaceTemp);
					}
//					Report.info("After with d: " + defs.stream().map(MemTemp::toString).collect(Collectors.joining(",")) + " s: " + uses.stream().map(MemTemp::toString).collect(Collectors.joining(",")));
				}

				AsmOPER newOper = new AsmOPER(newInstrString, new Vector<>(uses), new Vector<>(defs), null);

				if(instr.uses().contains(spillTemp)) {

					/*
					* SETL T2,c1 ->
					*
					* SET T2',c1 (using)
					* SET T2*,offset
					* ...
					* STO T2',FP,T2*
					* */
					code.instrs.add(index, newOper);
					code.instrs.add(index, new AsmOPER(  // add STO for uses
							Instructions.LDO + " `s0,$253,`s0",
							new Vector<>(List.of(offsetTemp)),
							new Vector<>(List.of(replaceTemp)),
							null));
					code.instrs.addAll(index, newOffsetInstructions);  // add instruction for storing offset
				}
				else if (instr.defs().contains(spillTemp)) {

					/*
					    ADD T4,T2,c2 ->

					    SET T2* offset
						LDO T2' FP T2*
						ADD T4, T2', c2
					 * */

					code.instrs.add(index, new AsmOPER(  // add LDO for defs
							Instructions.STO + " `d0,$253,`s0",
							new Vector<>(List.of(offsetTemp)),
							new Vector<>(List.of(replaceTemp)),
							null));
					code.instrs.add(index, newOper);
					code.instrs.addAll(index, newOffsetInstructions);  // add instruction for storing offset
				}

				code.instrs.remove(instr);  // remove instruction with spilled temp
//				Report.info("Old=" +spillTemp+ ". New=" + replaceTemp + " offset: " + offsetTemp);
			}
		}

		spillWorklist.clear();  // clear the list
		initial.clear();  // clear initial list
		functionToTempsMap.clear();  // clear list with functions and temps
//		initial.addAll(union(union(coloredNodes, coalescedNodes), newTemps));
		coloredNodes.clear();  // necessary from the book
		coalescedNodes.clear();  // necessary from the book
		Report.info("Rewrite program. SpilledN=" + spilledNodes.size());
	}

//	private List<AsmOPER> generateInstructionForNumber(MemTemp temp, Long value) {
//
//		List<AsmOPER> result = new ArrayList<>();
//
//		List<String> opers = new ArrayList<>(List.of(Instructions.SETH, Instructions.SETMH, Instructions.SETML, Instructions.SETL));
//		String hex = Long.toHexString(value);
//
//		if(hex.length() < 16)
//			hex = "0".repeat(16 - hex.length()) + hex;
//
//		for(int i = 0; i < 4; i++) {
//			String subString = hex.substring(i * 4, (i + 1) * 4);
//
//			AsmOPER oper = generateAsmOper(opers.get(i),
//					new InstrArgument(temp, InstrArgument.Type.Defn),
//					new InstrArgument("#" + subString));
//
//			result.add(oper);
//		}
//
//		return result;
//	}

	private Code findFunctionByTemp(MemTemp temp) {
		for(var tuple : functionToTempsMap) {
			if(tuple.value2.contains(temp)) {
				return tuple.value1;
			}
		}

		return null;
	}

	private void removeMoves() {
		for(Code code : AsmGen.codes) {
			code.instrs.removeIf(i -> i instanceof AsmMOVE);
		}
	}

	private void freezeMoves(MemTemp u) {
		for (var m : nodeMoves(u)) {
			// m = copy(x, y)
			MemTemp y = m.defs().getFirst();
			MemTemp x = m.uses().getFirst();


			MemTemp yAlias = getAlias(y);
			MemTemp uAlias = getAlias(u);
			MemTemp v = null;

			if(yAlias.equals(uAlias)) {
				v = getAlias(x);
			} else {
				v = yAlias;
			}

			activeMoves.remove(m);
			frozenMoves.add(m);
			if(freezeWorklist.contains(v) && nodeMoves(v).isEmpty()) {
				freezeWorklist.remove(v);
				simplifyWorklist.add(v);
			}
		}

	}


	/**
	 * Function assigns the rest of nodes on 0 degree and adjList to avoid NullReferenceException
	 */
	private void assignEmptyNodes() {
		for(var node : initial) {
			Integer degreeValue = degree.get(node);
			if(degreeValue == null) {
//				Report.warning("Degree is null: " + node);
				degree.put(node, 0);
			}

			adjList.computeIfAbsent(node, k -> new HashSet<>());
		}
	}

	private void printGraph(Collection<MemTemp> keys) {
//		Report.info("Interference graph:");
		for(MemTemp key : keys) {
			HashSet<MemTemp> list = adjList.get(key);
			if(list == null) list = new HashSet<>();

			String str = list.stream().map(MemTemp::toString).collect(Collectors.joining(", "));
//			Report.info(key + "(" + (degree.get(key)) + ") : " + str);
		}
	}

	/**
	 * @param code - block of code
	 * @return elements that remain after program execution
	 */
	private HashSet<MemTemp> getLiveOut(Code code) {

		// get return instruction
		for(AsmInstr instr : code.instrs.reversed()) {
			if(instr instanceof AsmMOVE oper && oper.instr().contains(Instructions.ADD) && !oper.defs().isEmpty()) {
//				Report.info("Found liveout " + instr);
				return new HashSet<>(oper.defs());
			}
		}

		// otherwise - an empty list
		return new HashSet<>();
	}

	/**
	 * Add edge of interference graph
	 * @param u, v are neighbors
	 */
	private void addEdge(MemTemp u, MemTemp v) {
		if(containsInAdjSet(u, v) || u == v) {
			return;
		}

		if(!preColored.contains(u)) {
			addEdgeForNode(u, v);
		}

		if(!preColored.contains(v)) {
			addEdgeForNode(v, u);
		}
	}

	// Function for addEdge()
	private void addEdgeForNode(MemTemp dst, MemTemp src) {
		if(dst == null || src == null)
			throw new Report.Error("In addEdgeForNode, Dst or Src is null! " + dst + " : " + src);

		HashSet<MemTemp> adjListToAppend = adjList.get(dst);
		if(adjListToAppend == null) adjListToAppend = new HashSet<>();

		if(adjListToAppend.contains(src)) {
			return;
		}

		adjListToAppend.add(src);
		adjList.put(dst, adjListToAppend);

		Integer degreeValue = degree.get(dst);
		if(degreeValue == null) degreeValue = 0;

		degree.put(dst, degreeValue + 1);
	}



	// get all the moves: active + worklist
	private List<AsmMOVE> nodeMoves(MemTemp n) {
		return intersection(moveList.get(n), union(activeMoves, worklistMoves));
	}

	private boolean isMoveRelated(MemTemp node) {
		return !nodeMoves(node).isEmpty();
	}

	private boolean ok(MemTemp t, MemTemp r) {
		return degree.get(t) < K || preColored.contains(t) || containsInAdjSet(t, r);
	}


	// return if the number of high value nodes is lower than K
	private boolean conservative(List<MemTemp> nodes) {
		int k = 0;
		for(var n : nodes) {
			if(degree.get(n) >= K) {
				k++;
			}
		}

		return k < K;
	}

	/**
	 * if u is not precolored, not move related and its degree is less than K
	 * then we remove it from freezeWorklist and add to simplifyWorklist
	 * @param u temp
	 */
	private void addWorklist(MemTemp u) {

		Integer degreeValue = degree.get(u);
		if(degreeValue == null) {
			Report.warning("AddWorklist. degree value is null: " + u);
			degreeValue = 0;
		}

		if(!preColored.contains(u) && !isMoveRelated(u) && degreeValue < K) {
			freezeWorklist.remove(u);
			simplifyWorklist.add(u);
		}
	}


	/**
	 * @param n node
	 * @return all neighbors nodes to node n without elements from selectStack and coalescedNodes
	 */
	private List<MemTemp> adjacent(MemTemp n) {
		List<MemTemp> adjacent  = new ArrayList<>(adjList.get(n));
		adjacent.removeAll(union(selectStack, coalescedNodes));
		return adjacent;
	}

	private void decrementDegree(MemTemp m) {
		Integer degreeValue = degree.get(m);
		if(degreeValue == 0) {
//			Report.warning("decrementDegree. degree  is 0: " + m);
			return;
		}

		// decrement degree
		degree.put(m, degreeValue-1);
		if(degreeValue != K) return;

		// enable moves (from active to worklist moves)
		enableMoves(union(List.of(m), adjacent(m)));
		spillWorklist.remove(m); // remove from spills and add to freeze or simplify depending on the situation
		if(isMoveRelated(m)) {
//			Report.info("DD of " + m + " Is to FR");
			freezeWorklist.add(m);
		}
		else {
//			Report.info("DD of " + m + " Is to SS");
			simplifyWorklist.add(m);
		}
	}


	private void enableMoves(List<MemTemp> nodes) {
		for(var n : nodes) {
			for(var m : nodeMoves(n)) {
				activeMoves.remove(m);
				worklistMoves.add(m);
			}
		}
	}

	private MemTemp getAlias(MemTemp n) {
		return coalescedNodes.contains(n)
				? getAlias(alias.get(n))
				: n;
	}

	/**
	 * Coalesces u and v
	 * @param u temp
	 * @param v temp
	 */
	private void combine(MemTemp u, MemTemp v) {
		if(freezeWorklist.contains(v)) {
			freezeWorklist.remove(v);
		}
		else {
			spillWorklist.remove(v);
		}

		coalescedNodes.add(v);

		alias.put(u, v);
		alias.put(v, u);

		moveList.put(u, new HashSet<>(union(moveList.get(u), moveList.get(v))));
		enableMoves(List.of(v));

		for(var t : adjacent(v)) {
			addEdge(t, u);
			decrementDegree(t);
		}

		Integer degreeVal = degree.get(u);

		if(degreeVal >= K && freezeWorklist.contains(u)) {
			freezeWorklist.remove(u);
			spillWorklist.add(u);
		}
	}


	// ArrayList helpers
	private List<Integer> getRange(int from, int to) {
		List<Integer> result = new ArrayList<>();
		for(int i = from; i <= to; i++) {
			result.add(i);
		}

		return result;
	}

	private  <T> List<T> union(Collection<T> list1, Collection<T> list2) {
		Set<T> set = new HashSet<T>();

		if(list1 != null) set.addAll(list1);
		if(list2 != null) set.addAll(list2);
		return new ArrayList<T>(set);
	}

	private <T> List<T> intersection(Collection<T> list1, Collection<T> list2) {
		List<T> list = new ArrayList<T>();

		if(list1 == null || list2 == null)
			return list;

		for (T t : list1) {
			if (list2.contains(t)) {
				list.add(t);
			}
		}

		return list;
	}

	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			logger.begElement("code");
			logger.addAttribute("body", code.entryLabel.name);
			logger.addAttribute("epilogue", code.exitLabel.name);
			logger.addAttribute("tempsize", Long.toString(code.tempSize));
			code.frame.log(logger);
			logger.begElement("instructions");
			for (AsmInstr instr : code.instrs) {
				logger.begElement("instruction");
				logger.addAttribute("code", instr.toString(tempToReg));
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
