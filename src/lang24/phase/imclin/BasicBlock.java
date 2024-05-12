package lang24.phase.imclin;

import lang24.data.imc.code.stmt.ImcJUMP;
import lang24.data.imc.code.stmt.ImcLABEL;
import lang24.data.imc.code.stmt.ImcStmt;

import java.util.Vector;

public class BasicBlock {
    public ImcLABEL label;
    public Vector<ImcStmt> stmts;

    public BasicBlock(ImcLABEL label) {
        this.label = label;

    }
}