package lang24.phase.regall;

import java.util.*;
import java.util.function.Predicate;
import java.util.random.RandomGenerator;

import lang24.common.report.Report;
import lang24.data.mem.*;
import lang24.data.asm.*;
import lang24.phase.*;
import lang24.phase.asmgen.*;
import lang24.phase.livean.LiveAn;
import org.w3c.dom.ranges.Range;

/**
 * Register allocation.
 */
public class RegAll extends Phase {

	public static int K = 4;
	private LiveAn liveAn = new LiveAn();
	public RegAll() {
		super("regall");
		generateInitial();
	}

	/**
	 * Fills initial collection of non-precolored and not processed temps
	 */
	private void generateInitial() {
		for(var code : AsmGen.codes) {
			for(var instr : code.instrs) {
				initial.addAll(instr.defs());
				initial.addAll(instr.uses());
			}
		}

		Report.info("Generating initials has ended");
	}

	/** Mapping of temporary variables to registers. */
	public final HashMap<MemTemp, Integer> tempToReg = new HashMap<MemTemp, Integer>();

	private final List<MemTemp> preColored = new ArrayList<>();  // machine registers
	private final HashSet<MemTemp> initial = new HashSet<>();  // temp registers, not pre colored nor processed
	private final List<MemTemp> simplifyWorklist = new ArrayList<>(); // list of low-degree non MR nodes
	private final List<MemTemp> freezeWorklist = new ArrayList<>();  // low-degree MR nodes
	private final List<MemTemp> spillWorklist = new ArrayList<>();  // high-degree nodes
	private final List<MemTemp> spilledNodes = new ArrayList<>();  // nodes marked to spill during this round
	private final List<MemTemp> coalescedNodes = new ArrayList<>();  // registers that have been coalesced
	private final List<MemTemp> coloredNodes = new ArrayList<>();  // nodes successfully colored
	private final Stack<MemTemp> selectStack = new Stack<>();  // removed temp vars from the stack


	/*Moves*/
	private final List<MemTemp> coalescedMoves = new ArrayList<>();  // moves that have been coalesced
	private final List<MemTemp> constrainedMoves = new ArrayList<>();  // moves whose src and dst interfere
	private final List<MemTemp> frozenMoves = new ArrayList<>();  // moves that will no longer be considered for coalescing
	private final List<AsmInstr> worklistMoves = new ArrayList<>();  // moves enable for positive coalescing
	private final List<AsmInstr> activeMoves = new ArrayList<>();  // moves not yet ready for coalescing

	/*Other*/
	private final List<Tuple<MemTemp, MemTemp>> adjSet = new ArrayList<>();  // DON'T USE DIRECTLY. set of edges (u, v), if (u,v) is in adjSet then (v,u) is in the set
	private final HashMap<MemTemp, List<MemTemp>> adjList = new HashMap<>();  // adjacency list of the graph for each non precolored u. adjList[u] - a set of nodes that interfere with u
	private final HashMap<MemTemp, Integer> degree = new HashMap<>();  // array that contains degree for each node
	private final HashMap<MemTemp, List<AsmInstr>> moveList = new HashMap<>();  // mapping from a node to the list of moves it is associated with
	private final HashMap<MemTemp, MemTemp> alias = new HashMap<>();  // when a move (u, v) has been coalesced, and v put in coalescedNodes, then alias(v) = u.
	private final HashMap<MemTemp, Integer> color = new HashMap<>();  // color chosen for the algorithm for a node; for precolored nodes this is initialized to the given color


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

	// RegAll functions
	public void allocate() {

		liveAn.analysis();  // refill code chunks
		build();  // build interference graph
		makeWorklist();  // fill list of temps we will work with

		do {
			if(!simplifyWorklist.isEmpty()) simplify();
//			if(!worklistMoves.isEmpty()) coalesce();
//			if(!freezeWorklist.isEmpty()) freeze();
			if(!spillWorklist.isEmpty()) selectSpill();
		} while(simplifyWorklist.isEmpty() && worklistMoves.isEmpty() &&
				freezeWorklist.isEmpty() && spillWorklist.isEmpty());

		assignColors();  // select stage, map colors with temps
		if(!spilledNodes.isEmpty()) {  // if we have nodes we want to spill
			rewriteProgram();  // spill operation
			allocate();  // repeat
		}

		Report.info("Allocating has ended");
	}

	/**
	 * Build interference graph
	 */
	private void build() {
		for(Code block  : AsmGen.codes) {
			List<MemTemp> live = getLiveOut(block);  // get temps which survived in block

			Report.info("Instructions per block: " + block.instrs.size());

			for(AsmInstr instr : block.instrs.reversed()) {
//				if(instr instanceof AsmMOVE move) { todo: move-related stuff
//					live.removeAll(move.uses());
//					for (var n : union(move.defs(), move.uses())) {
//						List<AsmInstr> list = moveList.get(n);
//						if(list != null) {
//							list.add(move);
//							moveList.put(n, list);
//						}
//					}
//					worklistMoves.add(instr);
//				}

				live.addAll(instr.defs());
				for (var d : instr.defs()) {
					for(var l : live) {
						addEdge(l, d);  // so live and d are edges
					}
				}

				live.removeAll(instr.defs());
				live = new ArrayList<>(union(instr.uses(), live));
			}
		}


		Report.info("Building has ended: " + adjList.size());
	}

	/**
	 * @param code - block of code
	 * @return elements that remain after program execution
	 */
	private List<MemTemp> getLiveOut(Code code) {
		return new ArrayList<>(code.instrs.lastElement().out());
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

		List<MemTemp> adjListToAppend = adjList.get(dst);
		if(adjListToAppend != null) {
			adjListToAppend.add(src);
			adjList.put(dst, adjListToAppend);
		}

		Integer degreeValue = degree.get(dst);
		if(degreeValue != null) {
			degree.put(dst, degreeValue + 1);
		}
	}

	// Generate pre-defined nodes we work with
	private void makeWorklist() {
		for(var n : initial) {
			Integer degreeValue = degree.get(n);
			if(degreeValue != null && degreeValue >= K) {
				spillWorklist.add(n);
			}
//			else if (isMoveRelated(n)) {
//				freezeWorklist.add(n);
//			}
			else {
				simplifyWorklist.add(n);
			}
		}

		initial.clear();
		Report.info("Making worklist has ended");
	}


	private List<AsmInstr> nodeMoves(MemTemp n) {
		List<AsmInstr> union = union(activeMoves, worklistMoves);
		var movesList = moveList.get(n);
		return intersection(movesList, union);
	}

	private boolean isMoveRelated(MemTemp node) {
		return !nodeMoves(node).isEmpty();
	}


	private void simplify() {

		Report.info("Simplifying has Started");

		if(simplifyWorklist.isEmpty()) return;

		var n = simplifyWorklist.getLast();
		if(n == null) return;

		if(simplifyWorklist.isEmpty()) {
			return;
		}

		simplifyWorklist.removeLast();
		selectStack.push(n);

		List<MemTemp> adjacentList = adjacent(n);

		if(adjacentList == null) {
			Report.warning("Simplify: adjacentList is null");
			Report.info("Simplifying has ended");
			return;
		}

		for(var m : adjacentList)
			decrementDegree(m);

		Report.info("Simplifying has ended");
	}

	// Helper functions for simplify()
	private List<MemTemp> adjacent(MemTemp n) {
		var list = adjList.get(n);

		if(list == null) {
			Report.warning("adjacent: list is null");
			return null;
		}

		var adjacent = new ArrayList<>(list);
		adjacent.removeAll(union(selectStack, coalescedNodes));
		return adjacent;
	}

	private void decrementDegree(MemTemp n) {
		Integer degreeValue = degree.get(n);
		if(degreeValue == null) return;

		degree.put(n, degreeValue + 1);
		if(degreeValue != K) return;

		var nodes = adjacent(n);

		if(nodes == null) {
			Report.warning("decrementDegree: list is null");
			return ;
		}

		nodes.add(n);
		enableMoves(nodes);
		spillWorklist.remove(n);
		if(isMoveRelated(n))
			freezeWorklist.add(n);
		else
			simplifyWorklist.add(n);
	}


	private List<MemTemp> enableMoves(List<MemTemp> nodes) {
		for(var n : nodes) {
			for(var m : nodeMoves(n)) {
				if(!activeMoves.contains(m)) continue;

				activeMoves.remove(m);
				worklistMoves.add(m);
			}
		}
		return null;
	}

	private MemTemp getAlias(MemTemp n) {
		return coalescedNodes.contains(n)
				? getAlias(alias.get(n))
				: n;
	}

	private void selectSpill() {
		//todo: use heuristics?

		if(spillWorklist.isEmpty()) return;

		var m = spillWorklist.getLast();
		spillWorklist.remove(m);
		simplifyWorklist.add(m);
//		freezeMoves(m);
	}

//	private void freezeMoves(MemTemp u) {
//		for(var m : nodeMoves(u)) {
//			var v = getAlias(m) == getAlias(u)
//					? getAlias(m)
//					: getAlias(m);
//
//			//todo solve the problem
//
//			activeMoves.remove(m);
//			frozenMoves.add(m);
//
//			if(freezeWorklist.contains(v) && nodeMoves(v).isEmpty()) {
//				freezeWorklist.remove(v);
//				simplifyWorklist.add(v);
//			}
//		}
//	}

	private void coalesce() {

	}


	/**
	 * select stage, map colors with temps
	 */
	private void assignColors() {
		while(!selectStack.isEmpty()) {
			var n = selectStack.pop();
			var okColors = getRange(0, K-1);

			var interList = adjList.get(n);
			if(interList == null) {
				Report.warning("AssignColors: interference list is null");
				continue;
			}

			for(var w : interList) {
				var list = union(coloredNodes, preColored);

				var alias = getAlias(w);
				if(list.contains(alias)){
					okColors.remove(color.get(alias));
				}

				if(okColors.isEmpty()) {
					spillWorklist.add(n);
				}
				else {
					coloredNodes.add(n);
				}

				if(!okColors.isEmpty()) {
					var c = okColors.getLast();
					color.put(n, c);
					for(var node : coalescedNodes) {
						color.put(node, color.get(getAlias(node)));
					}
				}


			}
		}

		Report.info("Assigning of colors has ended. Colors: " + color.size());
	}

	/**
	 * If AssignColors spills, then RewriteProgram allocates memory locations
	 * for the spilled temporaries and inserts store and fetch instructions to
	 * access them.
	 */
	private void rewriteProgram(){

		// todo: allocate newTemps

		spilledNodes.clear();
		initial.clear();
//		initial.addAll(union(union(coloredNodes, coalescedNodes), newTemps));
		coloredNodes.clear();
		coalescedNodes.clear();
	}

	// ArrayList helpers
	private List<Integer> getRange(int from, int to) {
		List<Integer> result = new ArrayList<>();
		for(int i = from; i <= to; i++) {
			result.add(i);
		}

		return result;
	}

	private  <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();
		set.addAll(list1);
		set.addAll(list2);
		return new ArrayList<T>(set);
	}

	private <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();
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
