package lang24.data.mem;

import lang24.common.logger.*;

/**
 * An access to a variable relative to an (unspecified) base address.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class MemRelAccess extends MemAccess {

	/**
	 * Offset of a variable relative to a base address, i.e., positive for
	 * parameters and record components, negative for local variables.
	 */
	public final long offset;

	/**
	 * The variable's static depth (-1 for record components, otherwise the static
	 * depth of the function within which the variable is defined).
	 */
	public final long depth;

	/**
	 * Constructs a new relative access.
	 * 
	 * @param size   The size of the variable.
	 * @param offset Offset of a variable relative to a base address.
	 * @param depth  The variable's static depth (-1 for record components).
	 */
	public MemRelAccess(long size, long offset, long depth) {
		super(size);
		this.offset = offset;
		this.depth = depth;
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("access");
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("offset", Long.toString(offset));
		if (depth >= 0)
			logger.addAttribute("depth", Long.toString(depth));
		logger.endElement();
	}

}