package lang24.data.imc.code;

import lang24.common.logger.*;
import lang24.data.imc.visitor.*;

/**
 * Intermediate code instruction.
 */
public abstract class ImcInstr implements Loggable {

	public abstract <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg);

}