package lang24.phase.imcgen;

import lang24.common.report.Report;
import lang24.data.ast.tree.AstNode;
import lang24.data.ast.tree.AstNodes;
import lang24.data.ast.tree.defn.AstDefn;
import lang24.data.ast.tree.defn.AstFunDefn;
import lang24.data.ast.tree.defn.AstTypDefn;
import lang24.data.ast.tree.defn.AstVarDefn;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.AstArrType;
import lang24.data.ast.tree.type.AstPtrType;
import lang24.data.ast.tree.type.AstRecType;
import lang24.data.ast.visitor.AstFullVisitor;
import lang24.data.imc.code.ImcInstr;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.code.stmt.*;
import lang24.data.mem.*;
import lang24.data.type.*;
import lang24.phase.memory.Memory;
import lang24.phase.seman.SemAn;

import java.util.Arrays;
import java.util.Vector;

public class ImcGenerator implements AstFullVisitor<ImcInstr, ImcGenerator.ImcArgument> {

    //*
    // Also ImcGenerator stores return values
    // *//

    private static final ImcExpr UNDEFINED_EXPR = new ImcCONST(-666);
    private MemTemp currentFramePointer = null;

    @Override
    public ImcInstr visit(AstNodes<? extends AstNode> nodes, ImcArgument arg) {

        Report.info(nodes, "Definitions processing has started");
        Vector<AstFunDefn> funcs = new Vector<>();

        for(var node : nodes) {
            if(node instanceof AstFunDefn)
                funcs.add((AstFunDefn) node);
        }

        Report.info(nodes, "Funcs were finished: " + funcs.size());

        if(!funcs.isEmpty()) {
            for(AstFunDefn function : funcs) {
                function.accept(this, arg);
            }
        }


        Report.info("Funcs were added: " + funcs.size());

        return new ImcSTMTS(new Vector<>());
    }


    @Override
    public ImcInstr visit(AstBinExpr binExpr, ImcArgument arg) {
        ImcExpr leftPart = (ImcExpr)binExpr.fstExpr.accept(this, arg);
        ImcExpr rightPart = (ImcExpr)binExpr.sndExpr.accept(this, arg);

        ImcBINOP.Oper oper = switch (binExpr.oper) {
            case OR -> ImcBINOP.Oper.OR;
            case AND -> ImcBINOP.Oper.AND;
            case EQU -> ImcBINOP.Oper.EQU;
            case NEQ -> ImcBINOP.Oper.NEQ;
            case LTH -> ImcBINOP.Oper.LTH;
            case GTH -> ImcBINOP.Oper.GTH;
            case LEQ -> ImcBINOP.Oper.LEQ;
            case GEQ -> ImcBINOP.Oper.GEQ;
            case ADD -> ImcBINOP.Oper.ADD;
            case SUB -> ImcBINOP.Oper.SUB;
            case MUL -> ImcBINOP.Oper.MUL;
            case DIV -> ImcBINOP.Oper.DIV;
            case MOD -> ImcBINOP.Oper.MOD;
        };

        return ImcGen.exprImc.put(binExpr, new ImcBINOP(oper, leftPart, rightPart));
    }



    @Override
    public ImcInstr visit(AstFunDefn funDefn, ImcArgument arg) {

        currentFramePointer = Memory.frames.get(funDefn).FP;

        Report.info(funDefn, "Function definition " + funDefn.name + " has started");
        // Add entry and exit labels
        ImcGen.entryLabel.put(funDefn, new MemLabel());
        ImcGen.exitLabel.put(funDefn, new MemLabel());

        ImcArgument argument = new ImcArgument(funDefn);

        if (funDefn.pars != null)
            funDefn.pars.forEach(a -> a.accept(this, argument));
        if (funDefn.stmt != null)
            funDefn.stmt.accept(this, argument);
        if (funDefn.defns != null)
            funDefn.defns.accept(this, argument);
        funDefn.type.accept(this, argument);


        return null;
    }

    @Override
    public ImcInstr visit(AstFunDefn.AstRefParDefn refParDefn, ImcArgument arg) {
        return AstFullVisitor.super.visit(refParDefn, arg);
    }

    @Override
    public ImcInstr visit(AstFunDefn.AstValParDefn valParDefn, ImcArgument arg) {
        return AstFullVisitor.super.visit(valParDefn, arg);
    }

    //** Expressions **

    @Override
    public ImcInstr visit(AstArrExpr arrExpr, ImcArgument arg) {
        ImcExpr arrArrExpr = (ImcExpr)arrExpr.arr.accept(this, arg);
        ImcExpr idxExpr = (ImcExpr)arrExpr.idx.accept(this, arg);


        // We get NameExpr in 90% of cases form arrArrExpr.
        // It means that we can get size of element fom that variable
        AstDefn arrayDefinition = SemAn.definedAt.get(getNameExprAsSubExpr(arrExpr.arr));
        SemArrayType arrayType = (SemArrayType)SemAn.ofType.get(arrExpr.arr);
        MemAccess memAccess = getAccessToVariableOrParameter(arrayDefinition);

        if(memAccess == null)
            throw new Report.Error(arrExpr, "Access to Variable of array is wrong! Definition: " + arrayDefinition);

        if(memAccess.size == 0)
            throw new Report.Error(arrayDefinition, "Size of this definition is 0");

        Report.info("Arr expr: " + arrArrExpr);

        // it is possible if we have static and local variables

        //todo: replaced while with if
        if(arrArrExpr instanceof ImcMEM)
            arrArrExpr = ((ImcMEM) arrArrExpr).addr;

        // Task: We need to get a variable and understanding how to access to array.
        // We add memory of arrArrExpr to addr(idxExpr) * sizeof(arrayElement)
        // we divide Size of Var by length of array to get 1 element
        return ImcGen.exprImc.put(arrExpr,
                new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD,

                    arrArrExpr,
                    new ImcBINOP(ImcBINOP.Oper.MUL,

                            idxExpr ,
                            //todo: check: we removed "/ arrayType.size" but it must work too
                            new ImcCONST(arrExpr.arr instanceof AstSfxExpr
                                    ? 8
                                    : memAccess.size / arrayType.size)

        ))));
    }

    private AstNameExpr getNameExprAsSubExpr(AstExpr expr) {
        AstExpr x = expr;
        while (!(x instanceof AstNameExpr)) {
            if(x instanceof AstSfxExpr)
                x = ((AstSfxExpr) x).expr;
        }

        return (AstNameExpr) x;
    }

    /**
     * Just using DRY principle. Because accessing to pars and vars have the same API
     * @param definition definition of Variable or Parameter
     * @return MemAccess on Variable or Parameter. Based on type of definition
     */
    private MemAccess getAccessToVariableOrParameter(AstDefn definition) {
        if(definition instanceof AstVarDefn)
            return Memory.varAccesses.get((AstVarDefn) definition);

        if(definition instanceof AstFunDefn.AstParDefn)
            return Memory.parAccesses.get((AstFunDefn.AstParDefn) definition);

        if(definition instanceof AstRecType.AstCmpDefn)
            return Memory.cmpAccesses.get((AstRecType.AstCmpDefn) definition);

        return null;
    }

    @Override
    public ImcInstr visit(AstAtomExpr atomExpr, ImcArgument arg) {

        // if we have Int, we put CONST
        if(atomExpr.type == AstAtomExpr.Type.INT) {
            return ImcGen.exprImc.put(atomExpr, new ImcCONST(Long.parseLong(atomExpr.value)));
        }

        // As we don't have true nad false in assembler, we use 0 and 1 constants
        if(atomExpr.type == AstAtomExpr.Type.BOOL) {
            int boolValue = atomExpr.value.equals("true") ? 1 : 0;
            return ImcGen.exprImc.put(atomExpr, new ImcCONST(boolValue));
        }

        // if we have string, we just put label of memory there
        if(atomExpr.type == AstAtomExpr.Type.STR) {
            MemLabel memLabel = Memory.strings.get(atomExpr).label;
            return ImcGen.exprImc.put(atomExpr, new ImcNAME(memLabel));
        }

        //todo: to be sure that we store CHARs as const
        //todo: add hexa converting
        if(atomExpr.type == AstAtomExpr.Type.CHAR) {
            long value = 0;
            
            Report.info(atomExpr, "String: " + atomExpr.value);

            if(atomExpr.value.contains("\\n") || atomExpr.value.contains("\\0A")) {
                value = 10;
            }
//            else if(atomExpr.value.length() == 5 && atomExpr.value.charAt(1) == '\\'
//                    && Character.isUpperCase(atomExpr.value.charAt(2))
//                    && Character.isUpperCase(atomExpr.value.charAt(3)))
//            {
//                Report.info("Hex was found");
//                value = convertHexToAscii(atomExpr.value);
//            }
            else {
                value = (long) atomExpr.value.charAt(1);
            }

            return ImcGen.exprImc.put(atomExpr, new ImcCONST(value));
        }

        if(atomExpr.type == AstAtomExpr.Type.PTR) {
            return ImcGen.exprImc.put(atomExpr, new ImcCONST(0));
        }

        if(atomExpr.type == AstAtomExpr.Type.VOID) {
            return ImcGen.exprImc.put(atomExpr, UNDEFINED_EXPR);
        }

        return null;
    }

    private int convertHexToAscii(String string) {
        StringBuilder output = new StringBuilder();

        for (int i = 0; i < string.length(); i += 2) {
            String str = string.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return Integer.getInteger(output.toString());
    }

    @Override
    public ImcInstr visit(AstCallExpr callExpr, ImcArgument arg) {

        AstFunDefn functionDefinition = (AstFunDefn)SemAn.definedAt.get(callExpr);
        Vector<Long> offsets = new Vector<>();
        Vector<ImcExpr> arguments = new Vector<>();

        // Fill Arguments vectors
        if (callExpr.args != null) {

            // Add offsets
            offsets.add(0L);
            for(var par : functionDefinition.pars) {
                offsets.add(((MemRelAccess)Memory.parAccesses.get(par)).offset);
            }

            // Add arguments
            arguments.add(new ImcTEMP(currentFramePointer)); //todo: change it after, provide SL
            for(var argument : callExpr.args) {
                arguments.add((ImcExpr) argument.accept(this, arg));
            }
        }

        // Get MemFrame to access to label
        MemFrame memFrame = Memory.frames.get(functionDefinition);

        return ImcGen.exprImc.put(callExpr, new ImcCALL(memFrame.label, offsets, arguments));
    }


    @Override
    public ImcInstr visit(AstCastExpr castExpr, ImcArgument arg) {
        Report.info(castExpr, "Exploring expr: " + castExpr.expr);
        ImcExpr expr = (ImcExpr) castExpr.expr.accept(this, arg);

        // If we cast to char type. We do mod 256
        if(SemAn.isType.get(castExpr.type) instanceof SemCharType) {
            return ImcGen.exprImc.put(castExpr, new ImcBINOP(
                    ImcBINOP.Oper.MOD,
                    expr,
                    new ImcCONST(256)
            ));
        }

        Report.info("Casted");
        // Otherwise just put the expression
        return ImcGen.exprImc.put(castExpr, expr);
    }

    @Override
    public ImcInstr visit(AstCmpExpr cmpExpr, ImcArgument arg) {

        ImcExpr expr = (ImcExpr) cmpExpr.expr.accept(this, arg);

        // Mem access of component to get component's size after
        AstDefn definition = SemAn.definedAt.get(cmpExpr);
        MemAccess memAccess = getAccessToVariableOrParameter(definition);

        return ImcGen.exprImc.put(cmpExpr, new ImcMEM(new ImcBINOP(
                ImcBINOP.Oper.ADD,
                expr,
                new ImcCONST(memAccess.size)

        )));
    }

    @Override
    public ImcInstr visit(AstNameExpr nameExpr, ImcArgument arg) {

        // Important to get a variable / parameter access,
        // and we need to return
        AstDefn definition = SemAn.definedAt.get(nameExpr);
        // Get MemAccess.
        // Our definition can be variable or parameter. In other cases - we throw exception
        MemAccess memAccess = getAccessToVariableOrParameter(definition);
        if(memAccess == null) {
            throw new Report.Error(nameExpr, "Mem access of Name Expression " + nameExpr.name +" is null!");
        }


        ImcExpr memoryExpression = null;
        // Get MemExpr (an argument for ImcMEM)
        if(memAccess instanceof MemAbsAccess) {
            // if we have absolute access, we just create Name
            memoryExpression = new ImcNAME(((MemAbsAccess) memAccess).label);
            memoryExpression = new ImcMEM(memoryExpression);
        }
        else {
            Report.info("Depth of " + nameExpr.name + " is " + ((MemRelAccess)memAccess).depth);
            // Problem 1.
            // We have the next issue now: we access to parameters that are given from parent func wrongly
            // because we sue fp of parent function
            // todo: find method to get actual FP
//            ImcExpr temp = new ImcTEMP(currentFramePointer);
            // Problem 2:
            // We need to know where the variables/parameter was declared
            // We need to understand what we use: Parameter or Variable from the "definition"
            // if the definition is variable - we find its position and use. Otherwise - get from parameters

//            ImcExpr temp = definition instanceof AstVarDefn
//                    ? new ImcTEMP(getMemoryLocationPointer((MemRelAccess)memAccess, arg.function))
//                    : new ImcTEMP(currentFramePointer);

            // Problem 3.
            // We can also get parameters from parent functions
            // We have wrong depths
            ImcExpr temp = new ImcTEMP(getMemoryLocationPointer((MemRelAccess)memAccess, arg.function));

            // 2.
            // if we have par - plus, otherwise (var) - minus (1)
            // fst arg is Temp(FP) of function where the variable(par) was created
            // snd arg is CONST with a sign (1) and the value of offset of the var(par)
//            ImcExpr temp = new ImcTEMP(getMemoryLocationPointer((MemRelAccess)memAccess, arg.function));

            memoryExpression = new ImcBINOP(
                        ImcBINOP.Oper.ADD,
                        temp, //it was done for some specific reasons
                        new ImcCONST( ((MemRelAccess)memAccess).offset));

            // We want to see a Memory if we have a pointer on a parameter.
            // todo: Probably, it can be also a variable
            memoryExpression = new ImcMEM(memoryExpression);
        }

        return ImcGen.exprImc.put(nameExpr, memoryExpression);
    }

    /**
     *
     * @param memRelAccess variable which pointer we are looking for
     * @param function executing function
     * @return Memory Location Pointer on FP where Memory Access was appeared
     */
    public MemTemp getMemoryLocationPointer(MemRelAccess memRelAccess, AstFunDefn function) {
        MemFrame memFrame = Memory.frames.get(function);
        AstFunDefn parentFunction = function;

        Report.info("Function definition: " + function.name + " depth: " + memFrame.depth);

        int i = 0;
        while (memFrame.depth != memRelAccess.depth - 1 && i < 10) {
            parentFunction = Memory.parentFunctions.get(parentFunction);

            if(parentFunction == null)
                throw new Report.Error(function, "Function: " +function.name+" does not have a parent function " +
                        "while receiving Local variable offset:" + memRelAccess.offset +" of upper depth. Depths: " + memFrame.depth + ":" + memRelAccess.depth);

            Report.info(parentFunction, "Parent function: " + parentFunction.name);

            memFrame = Memory.frames.get(parentFunction);
            i++;
        }

        return memFrame.FP;
    }

    @Override
    public ImcInstr visit(AstPfxExpr pfxExpr, ImcArgument arg) {
        ImcExpr baseExpr = (ImcExpr)pfxExpr.expr.accept(this, arg);

        return switch (pfxExpr.oper) {
            case ADD -> throw new Report.Error(pfxExpr, "ImcGenerator. Unsupported operator +");
            case SUB -> ImcGen.exprImc.put(pfxExpr, new ImcUNOP( ImcUNOP.Oper.NEG, baseExpr));
            case NOT -> ImcGen.exprImc.put(pfxExpr, new ImcUNOP( ImcUNOP.Oper.NOT, baseExpr));
            case PTR -> ImcGen.exprImc.put(pfxExpr, baseExpr instanceof ImcMEM ? ((ImcMEM) baseExpr).addr : baseExpr); // we leave default expression if we have PTR or ADD
        };
    }

    @Override
    public ImcInstr visit(AstSfxExpr sfxExpr, ImcArgument arg) {
        sfxExpr.expr.accept(this, arg);

//        ImcExpr expr =
//        ImcExpr result = expr instanceof ImcMEM ? ((ImcMEM) expr).addr : expr;
        ImcExpr result = ImcGen.exprImc.get(sfxExpr.expr);

        //todo: changed
        //this line of code below was written to ensure that ImcTEMP will be wrapped with ImcMEM
        //as I heard before it is important if the variable we access was declared in another function
//        if(result instanceof ImcBINOP && ((ImcBINOP) result).fstExpr instanceof ImcTEMP) {
//            result = new ImcBINOP(((ImcBINOP) result).oper, new ImcMEM(((ImcBINOP) result).fstExpr), ((ImcBINOP) result).sndExpr);
//        }

        //todo: changed removed new ImcMEM()
        return ImcGen.exprImc.put(sfxExpr, new ImcMEM(result));
    }

    //todo: finish whenever after
//    @Override
//    public ImcInstr visit(AstSizeofExpr sizeofExpr, ImcArgument arg) {
//        return null;
//    }


    //** Statements **

    //we will use MEM for AstNameExpr
    @Override
    public ImcInstr visit(AstAssignStmt assignStmt, ImcArgument arg) {
        arg.command = ImcArgument.Command.STORE;
        ImcExpr dst = (ImcExpr) assignStmt.dst.accept(this, arg);

        Report.info("Source of assign");

        arg.command = ImcArgument.Command.LOAD;
        ImcExpr src = (ImcExpr) assignStmt.src.accept(this, arg);

        return ImcGen.stmtImc.put(assignStmt, new ImcMOVE(dst, src));
    }

//    private ImcMOVE moveDelegateWithMem(ImcExpr dst, ImcExpr src){
//        return new ImcMOVE(
//            delegateExprWithMem(dst),
//            delegateExprWithMem(src)
//        );
//    }

    private ImcExpr delegateExprWithMem(ImcExpr expr) {

        // if we have ImcName - return memory
        if(expr instanceof ImcNAME)
            return new ImcMEM(expr);

        // if we have not BinOP or Name - return expr
        if(!(expr instanceof ImcBINOP))
            return expr;

        return new ImcMEM(expr);

        // otherwise - we have BinOp. Traverse it. Find memories. (We need to find Temp or Name)
//        return (!(((ImcBINOP) expr).fstExpr instanceof ImcNAME)
//             && !(((ImcBINOP) expr).fstExpr instanceof ImcTEMP))
//                ? expr
//                : new ImcMEM(expr);
    }


    @Override
    public ImcInstr visit(AstBlockStmt blockStmt, ImcArgument arg) {
        Report.info(blockStmt, "Block statement of "+ arg.function.name+" processing has started");

        Vector<ImcStmt> statements = new Vector<>();
        if (blockStmt.stmts != null) {
            for (AstStmt stmt : blockStmt.stmts) {
                Report.info("Statement: " + stmt);
                statements.add((ImcStmt) stmt.accept(this, arg));
            }
        }

        Report.info("Creating stmts");
        return ImcGen.stmtImc.put(blockStmt, new ImcSTMTS(statements));
    }

    @Override
    public ImcInstr visit(AstExprStmt exprStmt, ImcArgument arg) {


        Report.info(exprStmt, "Expr Stmt: " + exprStmt.expr);
        ImcExpr expr = (ImcExpr) exprStmt.expr.accept(this, arg);

        Report.info(exprStmt, "Expression: " + expr);

        return ImcGen.stmtImc.put(exprStmt,
                new ImcESTMT(expr));
    }

    @Override
    public ImcInstr visit(AstIfStmt ifStmt, ImcArgument arg) {
        Vector<ImcStmt> statements = new Vector<>();

        ImcExpr condition = (ImcExpr)ifStmt.cond.accept(this, arg);

        MemLabel posLabel = new MemLabel();
        MemLabel negLabel = new MemLabel();
        MemLabel postIfStmtLabel = new MemLabel(); // label that will be executed after if statement

        statements.add(new ImcCJUMP(condition, posLabel, negLabel)); // add condition
        statements.add(new ImcLABEL(posLabel)); // add pos label
        statements.add((ImcStmt) ifStmt.thenStmt.accept(this, arg)); // add then stmt
        statements.add(new ImcJUMP(postIfStmtLabel)); // add return label
        statements.add(new ImcLABEL(negLabel)); // add neg label

        if (ifStmt.elseStmt != null) {
            statements.add(((ImcStmt)ifStmt.elseStmt.accept(this, arg))); // add else stmt
        }

        statements.add(new ImcLABEL(postIfStmtLabel)); // add post if stmt label

        Report.info("Creating stmts");

        return ImcGen.stmtImc.put(ifStmt, new ImcSTMTS(statements));
    }

    @Override
    public ImcInstr visit(AstReturnStmt retStmt, ImcArgument arg) {
        AstFunDefn funDefinition = SemAn.returnStatements.get(retStmt);
        MemFrame memFrame = Memory.frames.get(funDefinition);
        Report.info(retStmt, "Return statement of " + funDefinition.name );

        // add move the expression to RV of function
        ImcMOVE move = new ImcMOVE(
                new ImcTEMP(memFrame.RV),
                (ImcExpr) retStmt.expr.accept(this, arg));


        // add jump to function's epilogue
        ImcJUMP jump = new ImcJUMP(ImcGen.exitLabel.get(funDefinition));

        Report.info("Creating stmts");

        return ImcGen.stmtImc.put(retStmt,
                new ImcSTMTS(new Vector<>(Arrays.asList(move, jump))));
    }

    @Override
    public ImcInstr visit(AstWhileStmt whileStmt, ImcArgument arg) {
        Vector<ImcStmt> statements = new Vector<>();

        ImcExpr condition = (ImcExpr) whileStmt.cond.accept(this, arg);
        ImcStmt whileInstrStmt = (ImcStmt) whileStmt.stmt.accept(this, arg);

        MemLabel conditionLabel = new MemLabel();
        MemLabel whileStmtLabel = new MemLabel();
        MemLabel postWhileStmtLabel = new MemLabel();

        statements.add(new ImcLABEL(conditionLabel)); // add condition start point
        statements.add(new ImcCJUMP(condition, whileStmtLabel, postWhileStmtLabel)); // add cjump
        statements.add(new ImcLABEL(whileStmtLabel)); // add while statement point
        statements.add(whileInstrStmt); // add while statement execution
        statements.add(new ImcJUMP(conditionLabel)); // add necessary jump to condition
        statements.add(new ImcLABEL(postWhileStmtLabel)); // add end point

        Report.info("Creating stmts");

        return ImcGen.stmtImc.put(whileStmt, new ImcSTMTS(statements));
    }

    //** Types **
    // ...

    public class ImcArgument {

        public enum Command {
            LOAD,
            STORE
        }

        public AstFunDefn function;
        public boolean isParam = false;
        public Command command;
        public ImcArgument(AstFunDefn function) {
            this.function = function;
        }
        public ImcArgument(AstFunDefn function, Command command) {
            this(function);
            this.command = command;
        }
    }
}
