package lang24.phase.asmgen;

import lang24.common.report.Report;
import lang24.data.asm.AsmInstr;
import lang24.data.asm.AsmLABEL;
import lang24.data.asm.AsmMOVE;
import lang24.data.asm.AsmOPER;
import lang24.data.imc.code.ImcInstr;
import lang24.data.imc.code.expr.*;
import lang24.data.imc.code.stmt.*;
import lang24.data.imc.visitor.ImcVisitor;
import lang24.data.mem.MemLabel;
import lang24.data.mem.MemTemp;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static lang24.phase.asmgen.AsmGen.addInstruction;
import static lang24.phase.asmgen.AsmGen.addSomeInstructions;

public class CodeGenerator implements ImcVisitor<MemTemp, ImcInstr> {

    public static MemTemp FP;
    public static MemTemp RV;
    private final String one = "1";
    private final String zero = "0";
    private final HashMap<MemTemp, ImcNAME> nameTempMap = new HashMap<>();  // todo: probably we should remove it



    public static AsmOPER generateAsmOper(String oper, InstrArgument... args) {
        Vector<MemTemp> defns = new Vector<>();
        Vector<MemTemp> uses = new Vector<>();
        Vector<MemLabel> jumps = new Vector<>();

        List<InstrArgument> argumentList = Arrays.stream(args).toList();

        int usesCount = 0;
        int defnsCount = 0;

        for(var arg : argumentList) {
            switch (arg.type) {
                case Use -> {
                    uses.add((MemTemp) arg.getValue());
                    arg.setId(usesCount++);
                }
                case Defn -> {
                    defns.add((MemTemp) arg.getValue());
                    arg.setId(defnsCount++);
                }
                case Label -> {
                    jumps.add((MemLabel) arg.getValue());
                }
            }
        }

        String instr = String.format("%s %s", oper,
                Arrays.stream(args)
                        .map(InstrArgument::toString)
                        .collect(Collectors.joining(",")));

        boolean isMove = (oper.equals(Instructions.ADD) &&                                    // ADD T1 T2 0
                            argumentList.get(0).getValue() instanceof MemTemp &&
                            argumentList.get(1).getValue() instanceof MemTemp &&
                            argumentList.get(2).getValue() instanceof String value &&
                            value.equals("0"));

        return isMove
                ? new AsmMOVE(instr, uses, defns)
                : new AsmOPER(instr, uses, defns, jumps);
    }

    public Object visit(ImcStmt stmt, ImcInstr arg) {
        return stmt.accept(this, arg);
    }


    @Override
    public MemTemp visit(ImcLABEL label, ImcInstr caller) {
        addInstruction(new AsmLABEL(label.label));
        return null;
    }

    @Override
    public MemTemp visit(ImcJUMP jump, ImcInstr caller) {
        AsmOPER jmpInstr = generateAsmOper(Instructions.JMP, new InstrArgument(jump.label));
        addInstruction(jmpInstr);
        return null;
    }

    @Override
    public MemTemp visit(ImcCJUMP cjump, ImcInstr caller) {
        MemTemp conditionResult = cjump.cond.accept(this, cjump);


        // will run if condition is positive???
        AsmOPER bpInstr = generateAsmOper(Instructions.BP,
                new InstrArgument(conditionResult),
                new InstrArgument(cjump.posLabel));

        addInstruction(bpInstr);
        return null;
    }


    @Override
    public MemTemp visit(ImcMOVE move, ImcInstr caller) {

        MemTemp dst, src = null;
        Object offset = null;
        String instr = Instructions.STO;

        // Pattern 1
        if(move.dst instanceof ImcMEM mem
                && mem.addr instanceof ImcBINOP binop
                && binop.oper == ImcBINOP.Oper.ADD) {

            dst = binop.fstExpr.accept(this, move);
            offset = binop.sndExpr.accept(this, move);
        }

        else {  // in this case dst, src are temps
            dst = move.dst.accept(this, move);  // we put "move" to make MEM understand extra behaviour
            offset = "0";
            instr = Instructions.ADD;
        }

        src = move.src.accept(this, move);  // don't put any pars

        AsmOPER oper = null;

        if(instr.equals(Instructions.STO)) {
            oper = generateAsmOper(instr,
                    new InstrArgument(src),
                    new InstrArgument(dst, InstrArgument.Type.Defn),
                    new InstrArgument(offset));
        } else {
            oper = generateAsmOper(instr,
                    new InstrArgument(dst, InstrArgument.Type.Defn),
                    new InstrArgument(src),
                    new InstrArgument(offset));
        }

        addInstruction(oper);
        return null;
    }



    @Override
    public MemTemp visit(ImcCALL call, ImcInstr caller) {

        AsmOPER PUSHJInstr = new AsmOPER("PUSHJ $8," + call.label.name, null, null, new Vector<>(List.of(call.label)));
        MemTemp stackPointerTemp = new MemTemp();

        AsmOPER addInstr = generateAsmOper(Instructions.ADD,
                new InstrArgument(stackPointerTemp, InstrArgument.Type.Defn),
                new InstrArgument(Instructions.SP),
                new InstrArgument("0"));

        addSomeInstructions(List.of(PUSHJInstr, addInstr));

        ///todo: figure out how to use $8

        return stackPointerTemp;
    }


    @Override
    public MemTemp visit(ImcBINOP binOp, ImcInstr caller) {

        MemTemp fstResult = binOp.fstExpr.accept(this, caller);
        MemTemp sndResult = binOp.sndExpr.accept(this, caller);

        MemTemp finalTemp = null;

        // first argument must be destination always
        // second argument must be result of the fstResult
        // third argument must be result of the fstResult

        // always definition. reuses temp if sub expr is const
        InstrArgument firstArgument = null;

        if(binOp.fstExpr instanceof ImcTEMP temp1 && temp1.temp.temp == RV.temp) {
            firstArgument = new InstrArgument(fstResult, InstrArgument.Type.Defn);
        }
        else if(binOp.sndExpr instanceof ImcTEMP temp2 && temp2.temp.temp == RV.temp) {
            firstArgument = new InstrArgument(fstResult, InstrArgument.Type.Defn);
        }
        else if(binOp.fstExpr instanceof ImcCONST) {
            firstArgument = new InstrArgument(fstResult, InstrArgument.Type.Defn);
        }
        else if(binOp.sndExpr instanceof ImcCONST) {
            firstArgument = new InstrArgument(sndResult, InstrArgument.Type.Defn);
        }
        else {
            firstArgument = new InstrArgument(new MemTemp(), InstrArgument.Type.Defn);
        }



        if(fstResult.equals(FP)) {
            Report.warning("FST RESULT is FP");
        }

        // it is our first result
        InstrArgument secondArgument = new InstrArgument(fstResult);
        InstrArgument thirdArgument  = new InstrArgument(sndResult);

        // widely used results
        AsmOPER cmpResult = generateAsmOper(Instructions.CMP, firstArgument, secondArgument, thirdArgument);


        switch (binOp.oper) {
            // Boolean responsibilities
            case OR  -> {
                AsmOPER result = generateAsmOper(Instructions.OR,
                        firstArgument, secondArgument, thirdArgument

                );

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case AND -> {

                AsmOPER result = generateAsmOper(Instructions.AND, firstArgument, secondArgument, thirdArgument);

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case NEQ -> {
                AsmOPER result = generateAsmOper(Instructions.CMP,
                        firstArgument, secondArgument, thirdArgument

                );

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case EQU -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ZSZ,
                        firstArgument, secondArgument, new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case LTH -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ZSN,  // zero or set if negative
                        firstArgument,
                        firstArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case GTH -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ZSP,
                        firstArgument,
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case LEQ -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ZSNP,
                        firstArgument,
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case GEQ -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ZSNN,
                        firstArgument,
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }

            // Arithmetical responsibilities
            case ADD -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ADD,
                        firstArgument, secondArgument, thirdArgument
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }

            case SUB -> {
                AsmOPER finalResult = generateAsmOper(Instructions.SUB,
                        firstArgument, secondArgument, thirdArgument
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }
            case MUL -> {
                AsmOPER finalResult = generateAsmOper(Instructions.MUL,
                        firstArgument, secondArgument, thirdArgument

                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }

            case DIV -> {
                AsmOPER finalResult = generateAsmOper(Instructions.DIV,
                        firstArgument, secondArgument, thirdArgument
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }
            case MOD -> {
                MemTemp newTemp = new MemTemp();

                AsmOPER divResult = generateAsmOper(Instructions.DIV,
                        new InstrArgument(newTemp, InstrArgument.Type.Defn),
                        new InstrArgument((MemTemp) fstResult ),  // todo: fix it
                        new InstrArgument(sndResult )
                );

                AsmOPER mulResult = generateAsmOper(Instructions.MUL,
                        new InstrArgument(newTemp, InstrArgument.Type.Defn),
                        new InstrArgument(newTemp),
                        new InstrArgument(sndResult )
                );

                AsmOPER subResult = generateAsmOper(Instructions.SUB,
                        new InstrArgument(newTemp, InstrArgument.Type.Defn),
                        new InstrArgument((MemTemp) fstResult ),
                        new InstrArgument(newTemp)
                );

                finalTemp = subResult.defs().firstElement();
                addSomeInstructions(List.of(divResult, mulResult, subResult));
            }
        }

        return finalTemp;
    }




    /**
     * We expect any Unary operation with parameters
     * .
     * sub is: Mem, Temp, Const?
     */
    @Override
    public MemTemp visit(ImcUNOP unOp, ImcInstr caller) {

        MemTemp result = unOp.subExpr.accept(this, unOp);
        MemTemp finalTemp = null;

        switch (unOp.oper) {
            case NOT -> {

                AsmOPER addResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument(result , InstrArgument.Type.Defn),
                        new InstrArgument(result ),
                        new InstrArgument(zero)
                );

                finalTemp = addResult.defs().firstElement();
                addInstruction(addResult);
            }
            case NEG -> {

                ImcCONST zeroConst = new ImcCONST(0);
                MemTemp zeroConstTemp = zeroConst.accept(this, null);

                AsmOPER addResult = generateAsmOper(Instructions.SUB,
                        new InstrArgument(result , InstrArgument.Type.Defn),
                        new InstrArgument(zeroConstTemp),
                        new InstrArgument(result)
                );

                finalTemp = addResult.defs().firstElement();
                addInstruction(addResult);
            }
        }

        return finalTemp;
    }


    @Override
    public MemTemp visit(ImcMEM mem, ImcInstr caller) {

        MemTemp src = null;
        Object offset = null;

        // Pattern 2
        if(mem.addr instanceof ImcBINOP binop
            && binop.oper == ImcBINOP.Oper.ADD) {

            src = binop.fstExpr.accept(this, mem);
            offset = binop.sndExpr.accept(this, mem);
        }
        else {
            src = mem.addr.accept(this, mem);
            offset = "0";
        }

        MemTemp dst = src.equals(FP) ? new MemTemp() : src;

        AsmOPER oper = generateAsmOper(Instructions.LDO,
                new InstrArgument(dst, InstrArgument.Type.Defn),
                new InstrArgument(src),
                new InstrArgument(offset)
        );

        addInstruction(oper);
        return dst;
    }


    @Override
    public MemTemp visit(ImcCONST constant, ImcInstr caller) {
        MemTemp memTemp = new MemTemp();

       addSomeInstructions(
               generateNumberInstructions(memTemp, constant.value));

        return memTemp;
    }

    public static List<AsmInstr> generateNumberInstructions(MemTemp memTemp, long value) {
        long first = value >> 0 & 0xFFFF;
        long second = value >> 16 & 0xFFFF;
        long third = value >> 32 & 0xFFFF;
        long fourth = value >> 48 & 0xFFFF;

        List<AsmInstr> list = new ArrayList<>();

        list.add(generateAsmOper(
                Instructions.SETL,
                new InstrArgument(memTemp, InstrArgument.Type.Defn),
                new InstrArgument(first)));

        list.add(generateAsmOper(
                Instructions.INCML,
                new InstrArgument(memTemp, InstrArgument.Type.Defn),
                new InstrArgument(second)));

        list.add(generateAsmOper(
                Instructions.INCMH,
                new InstrArgument(memTemp, InstrArgument.Type.Defn),
                new InstrArgument(third)));

        list.add(generateAsmOper(
                Instructions.INCH,
                new InstrArgument(memTemp, InstrArgument.Type.Defn),
                new InstrArgument(fourth)));

        return list;
    }

    @Override
    public MemTemp visit(ImcNAME name, ImcInstr caller) {
        MemTemp newMem = new MemTemp();

        AsmOPER setOper = generateAsmOper(Instructions.LDA,
                new InstrArgument(newMem, InstrArgument.Type.Defn),
                new InstrArgument(name.label));

        addInstruction(setOper);

        nameTempMap.put(newMem, name);

        return newMem;
    }

    @Override
    public MemTemp visit(ImcTEMP temp, ImcInstr caller) {
        return temp.temp;
    }
}
