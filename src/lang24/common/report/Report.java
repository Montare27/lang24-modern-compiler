package lang24.common.report;

/**
 * Generating reports.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class Report {

	/** (Unused but included to keep javadoc happy.) */
	private Report() {
		throw new Report.InternalError();
	}

	/** Counter of information messages printed out. */
	private static int numOfInfos = 0;

	/**
	 * Returns the number of information messages printed out.
	 * 
	 * @return The number of information messages printed out.
	 */
	public static int numOfInfos() {
		return numOfInfos;
	}

	/**
	 * Prints out an information message.
	 * 
	 * @param message The information message to be printed out.
	 */
	public static void info(final String message) {
		numOfInfos++;
		System.out.println(":-) " + message);
	}

	/**
	 * Prints out an information message relating to the specified part of the
	 * source file.
	 * 
	 * @param location The location the information message is related to.
	 * @param message  The information message to be printed.
	 */
	public static void info(final Locatable location, final String message) {
		numOfInfos++;
		System.out.println(":-) " + "[" + location.location() + "] " + message);
	}

	/** Counter of warnings printed out. */
	private static int numOfWarnings = 0;

	/**
	 * Returns the number of warnings printed out.
	 * 
	 * @return The number of warnings printed out.
	 */
	public static int numOfWarnings() {
		return numOfWarnings;
	}

	/**
	 * Prints out a warning.
	 * 
	 * @param message The warning message to be printed out.
	 */
	public static void warning(final String message) {
		numOfWarnings++;
		System.err.println(":-o " + message);
	}

	/**
	 * Prints out a warning relating to the specified part of the source file.
	 * 
	 * @param location The location the warning message is related to.
	 * @param message  The warning message to be printed out.
	 */
	public static void warning(final Locatable location, final String message) {
		numOfWarnings++;
		System.err.println(":-o " + "[" + location.location() + "] " + message);
	}

	/**
	 * An error.
	 * 
	 * Thrown whenever the program reaches a situation where any further computing
	 * makes no sense any more because of the erroneous input.
	 */
	@SuppressWarnings("serial")
	public static class Error extends java.lang.Error {

		/**
		 * Constructs a new error.
		 * 
		 * @param message The error message to be printed out.
		 */
		public Error(final String message) {
			super(":-( " + message);
		}

		/**
		 * Constructs a new error relating to the specified part of the source file.
		 * 
		 * @param location The location the error message is related to.
		 * @param message  The error message to be printed out.
		 */
		public Error(final Locatable location, final String message) {
			super(":-( " + "[" + location.location() + "] " + message);
		}

	}

	/**
	 * An internal error.
	 * 
	 * Thrown whenever the program encounters internal error.
	 */
	@SuppressWarnings("serial")
	public static class InternalError extends Error {

		/**
		 * Constructs a new internal error.
		 */
		public InternalError() {
			super("Internal error.");
			this.printStackTrace();
		}

	}

}