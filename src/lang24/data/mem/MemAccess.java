package lang24.data.mem;

import lang24.common.logger.*;

/**
 * Access to a variable.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public abstract class MemAccess implements Loggable {

	/** The size of the variable. */
	public final long size;

	/**
	 * Creates a new access to a variable.
	 * 
	 * @param size The size of the variable.
	 */
	public MemAccess(long size) {
		this.size = size;
	}

}