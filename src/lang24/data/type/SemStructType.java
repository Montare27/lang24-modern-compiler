package lang24.data.type;

import java.util.*;
import lang24.data.type.visitor.*;

/**
 * A struct type.
 * 
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class SemStructType extends SemRecordType {

	/**
	 * Constructs a new struct type.
	 * 
	 * @param compTypes Types of components.
	 */
	public SemStructType(final List<SemType> compTypes) {
		super(compTypes);
	}

	@Override
	public String toString() {
		final StringBuffer str = new StringBuffer();
		str.append("struct(");
		if (cmpTypes != null)
			str.append(cmpTypes.toString());
		str.append(")");
		return str.toString();
	}

	@Override
	public <Result, Argument> Result accept(SemVisitor<Result, Argument> visitor, Argument arg) {
		return visitor.visit(this, arg);
	}

}