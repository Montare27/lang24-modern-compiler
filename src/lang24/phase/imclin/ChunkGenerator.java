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
import lang24.data.mem.*;
import lang24.data.type.SemVoidType;
import lang24.phase.imcgen.ImcGen;
import lang24.phase.imcgen.ImcGenerator;
import lang24.phase.memory.Memory;
import lang24.phase.seman.SemAn;

import java.util.Arrays;
import java.util.Vector;

public class ChunkGenerator implements ImcVisitor<ImcInstr, ChunkArgument>, AstVisitor<ImcStmt, Object> {

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

    /**
     * Add return value NONE to function's RV
     */
    private void addNoneValueToFunction(AstFunDefn definition, MemLabel exitLabel) {
        MemFrame memFrame = Memory.frames.get(definition);

        ImcMOVE move = new ImcMOVE(
                new ImcTEMP(memFrame.RV),
                ImcGenerator.UNDEFINED_EXPR);

        ImcJUMP jump = new ImcJUMP(exitLabel);  // add jump to function's epilogue
        functionStatementVector.addAll(Arrays.asList(move, jump));
    }

    private ImcStmt processStatement(AstStmt statement) {
        ImcStmt stmts = ImcGen.stmtImc.get(statement);
        stmts.accept(this, null);
        return null;
    }


    @Override
    public ImcStmt visit(AstNodes<? extends AstNode> nodes, Object arg) {
       for(var function : nodes) {
           if(function instanceof AstFunDefn) {
                function.accept(this, arg);
           }
       }

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

        if(definition.stmt == null) return null;

        // Get entry and exit labels. Add entry to the vector
        MemLabel entryLabel = ImcGen.entryLabel.get(definition);
        MemLabel exitLabel = ImcGen.exitLabel.get(definition);
        functionStatementVector.add(new ImcLABEL(entryLabel));

        // Fill "functionStatementVector"
        definition.stmt.accept(this, null);

        if( ImcGen.entryLabel.get(definition) == null || ImcGen.exitLabel.get(definition) == null)
            throw new Report.Error(definition, "Entry label or Exit label is null");

        // add jump to return statement with value none if the function is of type void
        if(SemAn.ofType.get(definition) instanceof SemVoidType) {
            addNoneValueToFunction(definition, exitLabel);
        }

        // Add CodeChunk
        ImcLin.addCodeChunk(new LinCodeChunk(
                Memory.frames.get(definition),
                functionStatementVector,
                entryLabel,
                exitLabel));

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
        return processStatement(blockStmt);
    }

    //return ImcMoveStmt
    @Override
    public ImcStmt visit(AstAssignStmt assignStmt, Object arg) {
        return processStatement(assignStmt);
    }

    //return ImcESTMT
    @Override
    public ImcStmt visit(AstExprStmt exprStmt, Object arg) {
        return processStatement(exprStmt);
    }

    //return ImcSTMTS.
    @Override
    public ImcStmt visit(AstIfStmt ifStmt, Object arg) {
        return processStatement(ifStmt);
    }

    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstReturnStmt retStmt, Object arg) {
        return processStatement(retStmt);
    }

    //return ImcSTMTS
    @Override
    public ImcStmt visit(AstWhileStmt whileStmt, Object arg) {
        return processStatement(whileStmt);
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
    public ImcInstr visit(ImcSTMTS stmts, ChunkArgument visArg) {

        for(var stmt: stmts.stmts) {
            stmt.accept(this, visArg);
        }

        return null;
    }


    /**
     * Just add statement
     */
    @Override
    public ImcInstr visit(ImcJUMP jump, ChunkArgument visArg) {
        functionStatementVector.add(jump);
        return null;
    }


    /**
     * Just add statement
     */
    @Override
    public ImcInstr visit(ImcLABEL label, ChunkArgument visArg) {
        functionStatementVector.add(label);
        return label;
    }


    /**
     * Traverse expression.
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcCJUMP cjump, ChunkArgument visArg) {
        ImcExpr result = (ImcExpr) cjump.cond.accept(this, visArg);

        if(!(result instanceof ImcTEMP))  // we want to store ImcTemp in CJUMP condition expression
            result = storeComplexExpression(result, visArg);

        cjump.cond = result;
        functionStatementVector.add(cjump);
        return null;
    }


    /**
     * Traverse expression. We assume that it contains only CALL instruction.
     * Otherwise, it works as before
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcESTMT eStmt, ChunkArgument visArg) {
        ImcExpr expr = (ImcExpr) eStmt.expr.accept(this, new ChunkArgument(false));
        ImcMOVE newMove = new ImcMOVE(new ImcTEMP(new MemTemp()), expr);
        functionStatementVector.add(newMove);
        return null;
    }

    /**
     * Traverse expression.
     * It will return ImcMem with link to recently created variable
     * if sub expression is B, U or C
     */
    @Override
    public ImcInstr visit(ImcMOVE move, ChunkArgument visArg) {

        ImcExpr dst = null;

        // todo: added to fix pointer expressions
        if(move.dst instanceof ImcBINOP) {
            if(visArg == null) visArg = new ChunkArgument(true);
        }

        dst = (ImcExpr) move.dst.accept(this, visArg);

        ImcMOVE newSTMT = new ImcMOVE(
                dst,
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
    public ImcInstr visit(ImcBINOP binOp, ChunkArgument visArg) {
        ImcBINOP newExpr = new ImcBINOP(binOp.oper,
            (ImcExpr) binOp.fstExpr.accept(this, visArg),
            (ImcExpr) binOp.sndExpr.accept(this, visArg)
        );


        if(visArg != null && visArg.toCreateMove)
            return storeComplexExpression(newExpr, visArg);

        return newExpr;
    }

    /**
     * Almost the same implementation
     */
    @Override
    public ImcInstr visit(ImcUNOP unOp, ChunkArgument visArg) {
        ImcUNOP newExpr = new ImcUNOP(unOp.oper,
                (ImcExpr) unOp.subExpr.accept(this, visArg)
        );

        return storeComplexExpression(newExpr, visArg);
    }

    /**
     * Almost the same but we traverse all n arguments
     */
    @Override
    public ImcInstr visit(ImcCALL call, ChunkArgument visArg) {

        Vector<ImcExpr> args = new Vector<>();

        for(var arg : call.args) {
            ImcExpr result = (ImcExpr) arg.accept(this, visArg);

            // we want to store only ImcTemp's in the CALL to make asm instr easier
            if(!(result instanceof ImcTEMP))
                result = storeComplexExpression(result, visArg);

            args.add(result);
        }

        ImcCALL newExpr = new ImcCALL(call.label, call.offs, args);

        // if we run it from ESTMT we don't want to create a new part for CALL
        if(visArg != null && !visArg.toCreateMove) return newExpr;

        return storeComplexExpression(newExpr, visArg);
    }

    private ImcTEMP storeComplexExpression(ImcExpr expr, ChunkArgument argument) {
        ImcTEMP newMem = new ImcTEMP(new MemTemp());
        ImcMOVE imcMOVE = new ImcMOVE(newMem, expr);
        functionStatementVector.add(imcMOVE);
        return newMem;
    }

    /*
       Simple expressions. We solely return them
     */
    @Override
    public ImcInstr visit(ImcMEM mem, ChunkArgument visArg) {
        ImcExpr expr = (ImcExpr) mem.addr.accept(this, visArg);

        return new ImcMEM(expr);
    }

    @Override
    public ImcInstr visit(ImcCONST constant, ChunkArgument visArg) {
        return constant;
    }

    @Override
    public ImcInstr visit(ImcNAME name, ChunkArgument visArg) {
        return name;
    }

    @Override
    public ImcInstr visit(ImcSEXPR sExpr, ChunkArgument visArg) {
        return sExpr;
    }

    @Override
    public ImcInstr visit(ImcTEMP temp, ChunkArgument visArg) {
        return temp;
    }


}
