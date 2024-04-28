/**
 * A compiler for the LANG'24 programming language.
 * 
 * The compiler should be run as
 * 
 * <p>
 * <code>$ java lang24.Compiler </code><i>command-line-options...</i>
 * </p>
 * 
 * The following command line options are available:
 * 
 * <ul>
 * 
 * <li><code>--src-file-name=</code><i>file-name</i>: The name of the source
 * file, i.e., the file containing the code to be compiled.</li>
 * 
 * <li><code>--dst-file-name=</code><i>file-name</i>: The name of the
 * destination file, i.e., the file containing the compiled code (unless
 * specified the base name of the source file name is used).</li>
 * 
 * <li><code>--target-phase=</code><i>phase-name</i>: The name of the last phase
 * to be performed, or <code>none</code> or <code>all</code> (default).</li>
 * 
 * <li><code>--logged-phase=</code><i>phase-name</i>: The name of the phase the
 * report file is to be generated for, or <code>none</code> (default) or
 * <code>all</code>.</li>
 * 
 * <li><code>--xml=</code><i>prefix</i>: The prefix of the name of the generated
 * xml report files (unless specified the base name of the source file name is
 * used).</li>
 * 
 * <li><code>--xsl=</code><i>dir-name</i>: The directory where xsl templates
 * used by generated xml report files are stored.</li>
 * 
 * </ul>
 * 
 * The source file can be specified by its name only, i.e., without
 * <code>--src-file-name</code>. If the source file is not specified, the last
 * modified .p24 file found in the working directory is used.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
module lang24compiler {
	requires java.xml;
	requires antlr;
}
