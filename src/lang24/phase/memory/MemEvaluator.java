package lang24.phase.memory;

import java.lang.foreign.UnionLayout;
import java.util.*;
import java.util.stream.Collectors;

import lang24.common.report.Report;
import lang24.data.ast.tree.*;
import lang24.data.ast.tree.defn.*;
import lang24.data.ast.tree.expr.*;
import lang24.data.ast.tree.stmt.*;
import lang24.data.ast.tree.type.*;
import lang24.data.ast.visitor.*;
import lang24.data.lin.LinDataChunk;
import lang24.data.mem.*;
import lang24.data.type.*;
import lang24.data.type.visitor.*;
import lang24.phase.imclin.ImcLin;
import lang24.phase.seman.SemAn;

/**
 * Computing memory layout: stack frames and variable accesses.
 *
 * @author bostjan.slivnik@fri.uni-lj.si
 */
public class MemEvaluator implements AstFullVisitor<MemEvaluator.MemResult, MemArgument> {

    private static final MemTemp VOID_RETURN_VALUE = new MemTemp();
    private final List<AstFunDefn> calledFunctionsInFunction = new ArrayList<>();

    @Override
    public MemResult visit(AstNodes<? extends AstNode> nodes, MemArgument arg) {
        List<AstFunDefn> functions = new ArrayList<>();
        List<AstVarDefn> variables = new ArrayList<>();

        for(var node : nodes) {
            if(node instanceof AstVarDefn)
                variables.add((AstVarDefn) node);
            if(node instanceof AstFunDefn)
                functions.add((AstFunDefn) node);
        }

        // We need to look at the functions in 2 precedences:
        // 1. Get all the information except for statements:
        // 2. Run statements


        long localSize = 0;

        if(arg.precedence != 1) {
            for(AstVarDefn variable : variables) {
                MemResult memResult = variable.accept(this, arg);
                localSize += memResult.size;
                if(arg.depth != 0)
                    arg.offset -= memResult.size;
            }
        }

        functions.forEach(f -> f.accept(this, arg));

        return new MemResult(localSize);
    }

    @Override
    public MemResult visit(AstVarDefn varDefn, MemArgument arg) {
        MemResult typeSize = varDefn.type.accept(this, arg);

        MemAccess memAccess = null;

        if(arg.depth == 0) {
            memAccess = new MemAbsAccess(typeSize.size, new MemLabel(varDefn.name));
            ImcLin.addDataChunk(new LinDataChunk((MemAbsAccess) memAccess));
        }
        else {
            typeSize.size = calculateSizeWithPadding(typeSize.size);

            //WARNING BLOCK. Here was only arg.offset in the 2nd argument
            memAccess = new MemRelAccess(typeSize.size, -1 * (Math.abs(arg.offset) + typeSize.size), arg.depth);
        }

        Memory.varAccesses.put(varDefn, memAccess);
        return typeSize;
    }


    @Override
    public MemResult visit(AstTypDefn typDefn, MemArgument arg) {
        return typDefn.type.accept(this, arg);
    }


    @Override
    public MemResult visit(AstFunDefn functionDefinition, MemArgument arg) {

        long localSize = 0;
        long argsSize = 0;

        if(arg.precedence == 1) {
            if (functionDefinition.pars != null) {

                MemArgument argument = new MemArgument(arg.depth + 1, 8);

                for (AstFunDefn.AstParDefn par : functionDefinition.pars) {
                    MemResult parResult = par.accept(this, argument);
                    argument.offset += parResult.size;
                }
            }

            if (functionDefinition.defns != null) {
//                arg.depth++;

                MemArgument memArgument = new MemArgument(arg.depth + 1, 0);
                memArgument.precedence = 1;
                functionDefinition.defns.accept(this, memArgument);
            }

            return null;
        }

        if(arg.parentFunction != null) {
            Report.info(functionDefinition, "Parent func of " + functionDefinition.name + " is: " + arg.parentFunction.name);
            Memory.parentFunctions.put(functionDefinition, arg.parentFunction);
        }


        if (functionDefinition.defns != null) {
            // I removed offset
            MemArgument argument = new MemArgument(arg.depth + 1, 0);
            argument.parentFunction = functionDefinition;

            MemResult definitionsResult = functionDefinition.defns.accept(this, argument);
            localSize = definitionsResult.size;
        }

        List<AstFunDefn> nestedFunDefinitionList = null;

        if (functionDefinition.stmt != null) { // here we are looking exactly only on AstCallExpr
            MemArgument argument = new MemArgument(arg.depth +1, localSize);

            MemResult statementResult = functionDefinition.stmt.accept(this, argument);
            argsSize = statementResult.size;

            nestedFunDefinitionList = getChildFunctions(functionDefinition.defns == null ? new AstNodes<>() : functionDefinition.defns);
            if(argsSize != 0 && calledFunctionsInFunction.stream().anyMatch(nestedFunDefinitionList::contains)) {
                Report.info(functionDefinition, "Contains run nested function");
                argsSize += 8;
            }

            calledFunctionsInFunction.clear();
        }

        long size = localSize + argsSize + 8;

        MemLabel label = arg.depth == 0 ? new MemLabel(functionDefinition.name) : new MemLabel();
        MemFrame memFrame = new MemFrame(label, arg.depth, localSize, argsSize, size);

        if(SemAn.ofType.get(functionDefinition) instanceof SemVoidType)
            memFrame.RV = VOID_RETURN_VALUE;

        Memory.frames.put(functionDefinition, memFrame);

        return new MemResult(size);
    }

    private List<AstFunDefn> getChildFunctions(AstNodes<AstDefn> nodes) {
        List<AstDefn> definitions = new ArrayList<>();
        for(AstDefn node : nodes) definitions.add(node);

        return definitions.stream()
                .filter(d -> d instanceof AstFunDefn)
                .map(a -> (AstFunDefn)a)
                .collect(Collectors.toList());
    }

    @Override
    public MemResult visit(AstFunDefn.AstRefParDefn refParDefinition, MemArgument arg) {
        return visitParameter(refParDefinition, arg);
    }

    @Override
    public MemResult visit(AstFunDefn.AstValParDefn valParDefinition, MemArgument arg) {
        return visitParameter(valParDefinition, arg);
    }

    private MemResult visitParameter(AstFunDefn.AstParDefn parDefinition, MemArgument argument) {
        MemResult parTypeResult = parDefinition.type.accept(this, argument);
        Memory.parAccesses.put(parDefinition, new MemRelAccess(parTypeResult.size, argument.offset, argument.depth));
        return parTypeResult;
    }

    @Override
    public MemResult visit(AstArrExpr arrExpr, MemArgument arg) {
        MemResult arrResult = arrExpr.arr.accept(this, arg);
        MemResult idxResult = arrExpr.idx.accept(this, arg);

        return new MemResult(arrResult.size + idxResult.size);
    }

    @Override
    public MemResult visit(AstAtomExpr atomExpr, MemArgument arg) {
        if(atomExpr.type != AstAtomExpr.Type.STR) return new MemResult(0);

        long size = atomExpr.value.length() - 2;
        Memory.strings.put(atomExpr, new MemAbsAccess(size, new MemLabel()));

        return new MemResult(0);
    }

    @Override
    public MemResult visit(AstBinExpr binExpr, MemArgument arg) {
        MemResult fstResult = binExpr.fstExpr.accept(this, arg);
        MemResult sndResult = binExpr.sndExpr.accept(this, arg);
        return new MemResult(fstResult.size + sndResult.size);
    }

    @Override
    public MemResult visit(AstCastExpr castExpr, MemArgument arg) {
        MemResult typeResult = castExpr.type.accept(this, arg);
        MemResult exprResult = castExpr.expr.accept(this, arg);
        return new MemResult(typeResult.size + exprResult.size);
    }

    @Override
    public MemResult visit(AstCmpExpr cmpExpr, MemArgument arg) {
        return cmpExpr.expr.accept(this, arg);
    }

    @Override
    public MemResult visit(AstNameExpr nameExpr, MemArgument arg) {
        return new MemResult(0);
    }

    @Override
    public MemResult visit(AstPfxExpr pfxExpr, MemArgument arg) {
        return pfxExpr.expr.accept(this, arg);
    }

    @Override
    public MemResult visit(AstSfxExpr sfxExpr, MemArgument arg) {
        return sfxExpr.expr.accept(this, arg);
    }

    @Override
    public MemResult visit(AstSizeofExpr sizeofExpr, MemArgument arg) {
        return sizeofExpr.type.accept(this, arg);
    }

    @Override
    public MemResult visit(AstCallExpr callExpr, MemArgument arg) {
        long argsSize = 0;
        AstDefn definition = SemAn.definedAt.get(callExpr);

        if(!(definition instanceof AstFunDefn functionDefinition))
            throw new Report.Error(callExpr, "There is no function definition for. Result: " + definition);

        calledFunctionsInFunction.add(functionDefinition);

        if(( functionDefinition).pars != null) {
            for(AstFunDefn.AstParDefn par : (functionDefinition).pars) {
                MemAccess access = Memory.parAccesses.get(par);
                if(access == null)
                    throw new Report.Error(callExpr, "There is no Memory Access for function argument. " + par + ". Result: " + access);

                argsSize += access.size;
            }

            if (callExpr.args != null) {
                for(AstExpr argExpr : callExpr.args) {
                    MemResult argResult = argExpr.accept(this, arg);
                    argsSize += argResult.size;
                }
            }
        }

        return new MemResult(argsSize);
    }

    @Override
    public MemResult visit(AstBlockStmt blockStmt, MemArgument arg) {
        List<Long> argumentsSizes = new ArrayList<>();
        if (blockStmt.stmts != null) {
            for(AstStmt stmt : blockStmt.stmts) {
                MemResult stmtResult = stmt.accept(this, arg);
                argumentsSizes.add(stmtResult.size);
            }
        }

        long argsSize = argumentsSizes.stream().max(Long::compare).orElse(0L);
        return new MemResult(argsSize);
    }

    @Override
    public MemResult visit(AstAssignStmt assignStmt, MemArgument arg) {
        MemResult memResult1 = assignStmt.dst.accept(this, arg);
        MemResult memResult2 = assignStmt.src.accept(this, arg);
        return new MemResult(memResult1.size + memResult2.size);
    }

    @Override
    public MemResult visit(AstExprStmt exprStmt, MemArgument arg) {
        return exprStmt.expr.accept(this, arg);
    }

    @Override
    public MemResult visit(AstIfStmt ifStmt, MemArgument arg) {
        MemResult memIfResult = ifStmt.cond.accept(this, arg);
        MemResult memThenResult = ifStmt.thenStmt.accept(this, arg);
        MemResult memElseResult = ifStmt.elseStmt != null
                ? ifStmt.elseStmt.accept(this, arg)
                : new MemResult(0);

        return new MemResult(memThenResult.size + memIfResult.size + memElseResult.size);
    }

    @Override
    public MemResult visit(AstReturnStmt retStmt, MemArgument arg) {
        return retStmt.expr.accept(this, arg);
    }

    @Override
    public MemResult visit(AstWhileStmt whileStmt, MemArgument arg) {
        MemResult condResult = whileStmt.cond.accept(this, arg);
        MemResult stmtResult = whileStmt.stmt.accept(this, arg);
        return new MemResult(condResult.size = stmtResult.size);
    }

    @Override
    public MemResult visit(AstArrType arrType, MemArgument arg) {
        MemResult elemType = arrType.elemType.accept(this, arg);

        SemType type = SemAn.isType.get(arrType);
        if(!(type instanceof SemArrayType))
            throw new Report.Error(arrType, "Arr Type is not actually arr type: " + type);

        Report.info(arrType, "Array Size: " + ((SemArrayType) type).size * calculateSizeWithPadding(elemType.size) + "b");

        return new MemResult(((SemArrayType) type).size * calculateSizeWithPadding(elemType.size));
    }

    @Override
    public MemResult visit(AstAtomType atomType, MemArgument arg) {
        return switch (atomType.type) {
            case VOID -> new MemResult(0);
            case INT -> new MemResult(8);
            default -> new MemResult(1); // CHAR or BOOL
        };
    }

    @Override
    public MemResult visit(AstNameType nameType, MemArgument arg) {
        AstDefn typeDefinition = SemAn.definedAt.get(nameType);
        return typeDefinition.accept(this, arg);
    }

    @Override
    public MemResult visit(AstPtrType ptrType, MemArgument arg) {
        ptrType.baseType.accept(this, arg);
        return new MemResult(8);
    }

    @Override
    public MemResult visit(AstStrType strType, MemArgument arg) {

        long totalSize = 0;
        MemArgument argument = new MemArgument(arg.depth == 0 ? -1 : arg.depth, 0, strType);
        for (AstRecType.AstCmpDefn cmp : strType.cmps){
            MemResult cmpResult = cmp.accept(this, argument);
            totalSize += cmpResult.size;
            argument.offset -= cmpResult.size;
        }

        return new MemResult(totalSize);
    }

    @Override
    public MemResult visit(AstUniType uniType, MemArgument arg) {

        long maxSize = 0;

        MemArgument argument = arg.depth == 0
                ? new MemArgument(-1, 0, uniType)
                : new MemArgument(arg.depth, arg.offset, uniType);

        for (AstRecType.AstCmpDefn cmp : uniType.cmps){
            MemResult cmpResult = cmp.accept(this, argument);
            maxSize = Math.max(maxSize, cmpResult.size);
        }

        return new MemResult(maxSize);
    }


    /**
     *
     * @param type type we are looking for size
     * @return size of simple type
     */
    public long getSizeByType(SemType type){

        if(type instanceof SemNameType) return getSizeByType(type.actualType());

        if(type instanceof SemIntType || type instanceof SemPointerType) return 8;

        if(type instanceof SemCharType || type instanceof SemBoolType) return 1;

        if(type instanceof SemArrayType)
            return ((SemArrayType) type).size * getSizeByType(((SemArrayType) type).elemType);

        throw new Report.Error("Type: " + type + " is not available to calculate size!!!");
    }

    /**
     *
     * @param type actual type
     * @param arg additional argument of type MemArgument that serves offsets and depths
     * @return size of record type
     */
    public long getSizeByRecType(AstRecType type, MemArgument arg) {

        long totalSize = 0;
        MemArgument argument = new MemArgument(arg.depth == 0 ? -1 : arg.depth, 0, type);

        if(type instanceof AstUniType) {

            if(arg.depth != 0)
                argument.offset = arg.offset;

            for (AstRecType.AstCmpDefn cmp : type.cmps){
                MemResult cmpResult = cmp.accept(this, argument);
                totalSize = Math.max(totalSize, cmpResult.size);
            }
        }
        else if(type instanceof AstStrType) {
            for (AstRecType.AstCmpDefn cmp : type.cmps){
                MemResult cmpResult = cmp.accept(this, argument);
                totalSize += cmpResult.size;
                argument.offset -= cmpResult.size;
            }
        }
        else {
            throw new Report.Error("Type: " + type + " is not available to calculate size!!!");
        }

        return totalSize;
    }

    @Override
    public MemResult visit(AstRecType.AstCmpDefn cmpDefn, MemArgument arg) {
        MemResult size = cmpDefn.type.accept(this, arg);

        if(arg.more instanceof AstStrType)
            size.size = calculateSizeWithPadding(size.size);

        if(Memory.cmpAccesses.get(cmpDefn) == null)
            Memory.cmpAccesses.put(cmpDefn, new MemRelAccess(size.size, arg.offset, arg.depth));

        return size;
    }

    /**
     *
     * @param size size of variable
     * @return size with padding. next number that can be properly divided by 8
     */
    private long calculateSizeWithPadding(long size) {
        if(size % 8 == 0) return size;
        int i = 0;
        while((size + i) % 8 != 0) i++;
        return size + i;
    }

    public class MemResult {
        public long size;

        public MemResult(long size) {
            this.size = size;
        }
    }



}
