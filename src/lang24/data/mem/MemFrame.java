package lang24.data.mem;

import lang24.common.logger.*;

/**
 * A stack frame.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class MemFrame implements Loggable {

	/** The function's entry label. */
	public final MemLabel label;

	/** The function's static depth (global functions are at depth 0). */
	public final long depth;

	/** The size of the frame. */
	public final long size;

	/** The size of the block of local variables within a frame. */
	public final long locsSize;

	/** The size of the block of (call, i.e., outgoing) arguments within a frame. */
	public final long argsSize;

	/** The register to hold the frame pointer. */
	public final MemTemp FP;

	/** The register to hold the return value. */
	public final MemTemp RV;

	/**
	 * Constructs a new frame with no temporary variables and no saved registers.
	 * 
	 * @param label    The function's entry label.
	 * @param depth    The function's static depth.
	 * @param locsSize The size of the block of local variables within a frame.
	 * @param argsSize The size of the block of (call, i.e., outgoing) arguments
	 *                 within a frame.
	 * @param size     The size of the frame.
	 */
	public MemFrame(MemLabel label, long depth, long locsSize, long argsSize, long size) {
		this.label = label;
		this.depth = depth;
		this.locsSize = locsSize;
		this.argsSize = argsSize;
		this.size = size;
		this.FP = new MemTemp();
		this.RV = new MemTemp();
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("frame");
		logger.addAttribute("label", label.name);
		logger.addAttribute("depth", Long.toString(depth));
		logger.addAttribute("locssize", Long.toString(locsSize));
		logger.addAttribute("argssize", Long.toString(argsSize));
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("FP", FP.toString());
		logger.addAttribute("RV", RV.toString());
		logger.endElement();
	}

}