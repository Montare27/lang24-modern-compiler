package lang24.data.type.visitor;

import lang24.common.report.*;
import lang24.data.type.*;

/**
 * A semantic type visitor.
 * 
 * @param <Result>   The result type.
 * @param <Argument> The argument type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public interface SemVisitor<Result, Argument> {

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemVoidType voidType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemBoolType boolType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemCharType charType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemIntType intType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemArrayType arrType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemPointerType ptrType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemStructType strType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemUnionType uniType, Argument arg) {
		throw new Report.InternalError();
	}

	@SuppressWarnings({ "doclint:missing" })
	public default Result visit(SemNameType nameType, Argument arg) {
		throw new Report.InternalError();
	}

}