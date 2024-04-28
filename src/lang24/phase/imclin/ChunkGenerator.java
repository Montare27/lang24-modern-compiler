package lang24.phase.imclin;

import lang24.common.report.Report;
import lang24.data.ast.tree.AstNode;
import lang24.data.ast.tree.AstNodes;
import lang24.data.ast.tree.defn.AstFunDefn;
import lang24.data.ast.tree.defn.AstVarDefn;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.visitor.AstVisitor;
import lang24.data.imc.code.ImcInstr;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.code.stmt.*;
import lang24.data.imc.visitor.ImcVisitor;
import lang24.data.lin.LinCodeChunk;
import lang24.data.lin.LinDataChunk;
import lang24.data.mem.MemAbsAccess;
import lang24.data.mem.MemAccess;
import lang24.data.mem.MemLabel;
import lang24.data.mem.MemTemp;
import lang24.phase.imcgen.ImcGen;
import lang24.phase.memory.Memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ChunkGenerator implements ImcVisitor<ImcInstr, Object>, AstVisitor<ImcStmt, Object> {

    /*
        todo:
        1. dataChunks (+)
        2. codeChunks (+)
        2.5 Add BlockStmt (+)
        2.75 Add other statements (+)
        3. simplify Calls (+)
        4. simplify STMTS (+)
        5. simplify STMTEXPR (+)
        6. add value to RV (+)

     */

    /**
     * Vector where we store all statements per function
     */
    private final Vector<ImcStmt> functionStatementVector = new Vector<>();

    @Override
    public ImcStmt visit(AstNodes<? extends AstNode> nodes, Object arg) {
       List<AstFunDefn> functions = new ArrayList<>();

       for(var node : nodes) {
           if(node instanceof AstFunDefn)
               functions.add((AstFunDefn)node);
       }

       functions.forEach(f -> f.accept(this, arg));

        return null;
    }

    /**
     * Just fill DataChunks
     */
    @Override
    public ImcStmt visit(AstVarDefn varDefn, Object arg) {
        MemAccess memAccess = Memory.varAccesses.get(varDefn);

        if(memAccess instanceof MemAbsAccess)
            ImcLin.addDataChunk(new LinDataChunk((MemAbsAccess) memAccess));

        return null;
    }

    /**
     * Fills code chunks
     */
    @Override
    public ImcStmt visit(AstFunDefn definition, Object arg) {

        // Get entry and exit labels. Add entry to the vector
        MemLabel entryLabel = ImcGen.entryLabel.get(definition);
        MemLabel exitLevel = ImcGen.exitLabel.get(definition);
        functionStatementVector.add(new ImcLABEL(entryLabel));

        // Find new functions we put null if we want to get variables and arg - if functions
//        if(definition.defns != null)
//            definition.defns.accept(this, null);

        // Fill "functionStatementVector"
        if(definition.stmt != null)
            definition.stmt.accept(this, arg);

        if( ImcGen.entryLabel.get(definition) == null || ImcGen.exitLabel.get(definition) == null)
            throw new Report.Error(definition, "Entry label or Exit lavel is null");

        //Add exit to the vector after statements and definitions
        functionStatementVector.add(new ImcLABEL(exitLevel));

        // Add CodeChunk
        ImcLin.addCodeChunk(new LinCodeChunk(
                Memory.frames.get(definition),
                functionStatementVector,
                entryLabel,
                exitLevel));

        // Clear "functionStatementVector"
        functionStatementVector.clear();

        if(definition.defns != null)
            definition.defns.accept(this, arg);

        return null;
    }


    /**
     * AST Statements.
     * These visitors will called only if they are mentioned as "stmt" of AstFunDefn.
     * In 90% - AstBlockStmt is called
     * But everything is possible.
     * So in all functions below that traverse children of AstStmt interface
     * we solely call visitor for statements and collect the result in Vector<ImcStmt>
     * to use it after in AstFunDefn
     * **/


    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstBlockStmt blockStmt, Object arg) {
        ImcSTMTS stmts = (ImcSTMTS)ImcGen.stmtImc.get(blockStmt);

        stmts.accept(this, null);
        return null;
    }

    //return ImcMoveStmt
    @Override
    public ImcStmt visit(AstAssignStmt assignStmt, Object arg) {
        ImcMOVE stmt = (ImcMOVE)ImcGen.stmtImc.get(assignStmt);

        stmt.accept(this, null);
        return null;
    }

    //return ImcESTMT
    @Override
    public ImcStmt visit(AstExprStmt exprStmt, Object arg) {
        ImcStmt stmt = ImcGen.stmtImc.get(exprStmt);

        stmt.accept(this, null);
        return null;
    }

    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstIfStmt ifStmt, Object arg) {
        ImcSTMTS stmts = (ImcSTMTS)ImcGen.stmtImc.get(ifStmt);

        stmts.accept(this, null);
        return null;
    }

    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstReturnStmt retStmt, Object arg) {
        ImcSTMTS stmts = (ImcSTMTS)ImcGen.stmtImc.get(retStmt);

        stmts.accept(this, null);
        return null;
    }

    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstWhileStmt whileStmt, Object arg) {
        ImcSTMTS stmts = (ImcSTMTS)ImcGen.stmtImc.get(whileStmt);

        stmts.accept(this, null);
        return null;
    }

    /*
     * Finally below we only deal with ImcInstr implementations,
     * where our task is simplifying of canonical trees
     * .
     * **/

    /**
     * Just traverse sub-statements
     */
    @Override
    public ImcInstr visit(ImcSTMTS stmts, Object visArg) {

        for(var stmt: stmts.stmts) {
            stmt.accept(this, visArg);
        }

        return null;
    }


    /**
     * Just add statement
     */
    @Override
    public ImcInstr visit(ImcJUMP jump, Object visArg) {
        functionStatementVector.add(jump);
        return null;
    }


    /**
     * Just add statement
     */
    @Override
    public ImcInstr visit(ImcLABEL label, Object visArg) {
        functionStatementVector.add(label);
        return label;
    }


    /**
     * Traverse expression.
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcCJUMP cjump, Object visArg) {
        cjump.cond = (ImcExpr) cjump.cond.accept(this, visArg);
        functionStatementVector.add(cjump);
        return null;
    }


    /**
     * Traverse expression.
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcESTMT eStmt, Object visArg) {
        ImcESTMT newSTMT = new ImcESTMT((ImcExpr) eStmt.expr.accept(this, visArg));
        functionStatementVector.add(newSTMT);
        return null;
    }

    /**
     * Traverse expression.
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcMOVE move, Object visArg) {
        ImcMOVE newSTMT = new ImcMOVE(
                (ImcExpr) move.dst.accept(this, 1), // we throw 1 if we expect that there will be
                (ImcExpr) move.src.accept(this, visArg));

        functionStatementVector.add(newSTMT);
        return null;
    }

    /*
     * Work with ImcExpressions
     */

    /**
     * Traverses sub expressions
     * Adds to vector ImcMove with the instance on new Temporary variable
     */
    @Override
    public ImcInstr visit(ImcBINOP binOp, Object visArg) {
        ImcBINOP newExpr = new ImcBINOP(binOp.oper,
            (ImcExpr) binOp.fstExpr.accept(this, visArg),
            (ImcExpr) binOp.sndExpr.accept(this, visArg)
        );

        return storeComplexExpression(newExpr);
    }

    /**
     * Almost the same implementation
     */
    @Override
    public ImcInstr visit(ImcUNOP unOp, Object visArg) {
        ImcUNOP newExpr = new ImcUNOP(unOp.oper,
                (ImcExpr) unOp.subExpr.accept(this, visArg)
        );

        return storeComplexExpression(newExpr);
    }

    /**
     * Almost the same but we traverse all n arguments
     */
    @Override
    public ImcInstr visit(ImcCALL call, Object visArg) {

        Vector<ImcExpr> args = new Vector<>();

        for(var arg : call.args) {
            args.add((ImcExpr) arg.accept(this, visArg));
        }

        ImcCALL newExpr = new ImcCALL(
                call.label,
                call.offs,
                args
        );

        return storeComplexExpression(newExpr);
    }

    private ImcTEMP storeComplexExpression(ImcExpr expr) {
//        Report.info("MemTemp T" +  + " was created!");
        ImcTEMP newMem = new ImcTEMP(new MemTemp());

        ImcMOVE imcMOVE = new ImcMOVE(
                newMem,
                expr);

        functionStatementVector.add(imcMOVE);

        return newMem;
    }

    /*
        Simple expressions. We solely return them
     */

    @Override
    public ImcInstr visit(ImcCONST constant, Object visArg) {
        return constant;
    }

    @Override
    public ImcInstr visit(ImcMEM mem, Object visArg) {
        return mem;
    }

    @Override
    public ImcInstr visit(ImcNAME name, Object visArg) {
        return name;
    }

    @Override
    public ImcInstr visit(ImcSEXPR sExpr, Object visArg) {
        return sExpr;
    }

    @Override
    public ImcInstr visit(ImcTEMP temp, Object visArg) {
        return temp;
    }


}
