package lang24.phase.asmgen;

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
import java.util.stream.Collectors;

import static lang24.phase.asmgen.AsmGen.addInstruction;
import static lang24.phase.asmgen.AsmGen.addSomeInstructions;

public class CodeGenerator implements ImcVisitor<MemTemp, ImcInstr> {

    public static MemTemp FP;
    private boolean isMove = false;
    private final String one = "1";
    private final String zero = "0";
    private final HashMap<MemTemp, ImcNAME> nameTempMap = new HashMap<>();  // todo: probably we should remove it


    private AsmOPER generateAsmOper(String oper, InstrArgument... args) {
        Vector<MemTemp> defns = new Vector<>();
        Vector<MemTemp> uses = new Vector<>();
        Vector<MemLabel> jumps = new Vector<>();

        int usesCount = 0;
        int defnsCount = 0;

        for(var arg : args) {
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
                        .collect(Collectors.joining(", ")));


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

        AsmOPER bzpInstr = generateAsmOper(Instructions.BZP,
                new InstrArgument(conditionResult),
                new InstrArgument(cjump.posLabel));

        addSomeInstructions(Arrays.asList(
                bzpInstr,
                new AsmLABEL(cjump.negLabel),
                new AsmLABEL(cjump.posLabel)));

        return null;
    }


    @Override
    public MemTemp visit(ImcMOVE move, ImcInstr caller) {

        MemTemp dst, src = null;
        Object offset = null;

        // Pattern 1
        if(move.dst instanceof ImcMEM mem
                && mem.addr instanceof ImcBINOP binop
                && binop.oper == ImcBINOP.Oper.AND) {

            if(move.src instanceof ImcMEM) isMove = true;

            dst = binop.fstExpr.accept(this, move);
            offset = binop.sndExpr.accept(this, move);
        }
        else {
            dst = move.dst.accept(this, move);  // we put "move" to make MEM understand extra behaviour
            offset = "0";
        }

        src = move.src.accept(this, move);  // don't put any pars

        AsmOPER oper = generateAsmOper(Instructions.STO,
            new InstrArgument(src),
            new InstrArgument(dst, InstrArgument.Type.Defn),
            new InstrArgument(offset));

        isMove = false;

        addInstruction(new AsmMOVE(oper.instr(), oper.uses(), oper.defs()));
        return null;
    }



    @Override
    public MemTemp visit(ImcCALL call, ImcInstr caller) {

        MemTemp returnTemp = new MemTemp(); // a possible issue. maybe Here must be something else

        AsmOPER PUSHJInstr = generateAsmOper(Instructions.PUSHJ,
                new InstrArgument(returnTemp, InstrArgument.Type.Defn),
                new InstrArgument(call.label));

        MemTemp stackPointerTemp = new MemTemp();

        AsmOPER addInstr = generateAsmOper(Instructions.ADD,
                new InstrArgument(stackPointerTemp, InstrArgument.Type.Defn),
                new InstrArgument(Instructions.SP),
                new InstrArgument("0"));

        addSomeInstructions(List.of(PUSHJInstr, addInstr));

        return returnTemp;
    }


    @Override
    public MemTemp visit(ImcBINOP binOp, ImcInstr caller) {

        MemTemp fstResult = binOp.fstExpr.accept(this, caller);
        MemTemp sndResult = binOp.sndExpr.accept(this, caller);

        MemTemp finalTemp = null;

        InstrArgument secondArgument = fstResult.equals(FP)
                ? new InstrArgument(new MemTemp(), InstrArgument.Type.Defn)
                : new InstrArgument(fstResult);


        switch (binOp.oper) {
            // Boolean responsibilities
            case OR  -> {
                AsmOPER result = generateAsmOper(Instructions.OR,
                        new InstrArgument(fstResult , InstrArgument.Type.Defn),
                        secondArgument,
                        new InstrArgument(sndResult )
                );

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case AND -> {

                AsmOPER result = generateAsmOper(Instructions.AND,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(sndResult )
                );

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case NEQ -> {
                AsmOPER result = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                finalTemp = result.defs().firstElement();
                addInstruction(result);
            }
            case EQU -> {
                AsmOPER cmpResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                AsmOPER finalResult = generateAsmOper(Instructions.ZSZ,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case LTH -> {
                AsmOPER cmpResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                AsmOPER finalResult = generateAsmOper(Instructions.ZSN,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case GTH -> {
                AsmOPER cmpResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                AsmOPER finalResult = generateAsmOper(Instructions.ZSP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case LEQ -> {
                AsmOPER cmpResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                AsmOPER finalResult = generateAsmOper(Instructions.ZSNP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }
            case GEQ -> {
                AsmOPER cmpResult = generateAsmOper(Instructions.CMP,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument( sndResult )
                );

                AsmOPER finalResult = generateAsmOper(Instructions.ZSNN,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(one)
                );

                finalTemp = finalResult.defs().firstElement();
                addSomeInstructions(List.of(cmpResult, finalResult));
            }

            // Arithmetical responsibilities
            case ADD -> {
                AsmOPER finalResult = generateAsmOper(Instructions.ADD,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(sndResult )
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }

            case SUB -> {
                AsmOPER finalResult = generateAsmOper(Instructions.SUB,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(sndResult )
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }
            case MUL -> {
                AsmOPER finalResult = generateAsmOper(Instructions.MUL,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(sndResult )
                );

                finalTemp = finalResult.defs().firstElement();
                addInstruction(finalResult);
            }

            case DIV -> {
                AsmOPER finalResult = generateAsmOper(Instructions.DIV,
                        new InstrArgument((MemTemp) fstResult , InstrArgument.Type.Defn),
                         secondArgument,
                        new InstrArgument(sndResult )
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
                AsmOPER addResult = generateAsmOper(Instructions.SUB,
                        new InstrArgument(result , InstrArgument.Type.Defn),
                        new InstrArgument(zero),
                        new InstrArgument(result )
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
        return src;
    }


    @Override
    public MemTemp visit(ImcCONST constant, ImcInstr caller) {
        MemTemp memTemp = new MemTemp();

        List<String> opers = new ArrayList<>(List.of(Instructions.SETH, Instructions.SETMH, Instructions.SETML, Instructions.SETL));
        String hex = Long.toHexString(constant.value);

        if(hex.length() < 16)
            hex = "0".repeat(16 - hex.length()) + hex;

        for(int i = 0; i < 4; i++) {
            String subString = hex.substring(i * 4, (i + 1) * 4);

            AsmOPER oper = generateAsmOper(opers.get(i),
                    new InstrArgument(memTemp, InstrArgument.Type.Defn),
                    new InstrArgument("#" + subString));

            addInstruction(oper);
        }

        return memTemp;
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
