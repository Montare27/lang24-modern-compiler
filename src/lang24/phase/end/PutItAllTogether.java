package lang24.phase.end;

import lang24.common.report.Report;
import lang24.data.asm.AsmInstr;
import lang24.data.asm.AsmLABEL;
import lang24.data.asm.AsmOPER;
import lang24.data.asm.Code;
import lang24.data.lin.LinDataChunk;
import lang24.phase.asmgen.AsmGen;
import lang24.phase.imclin.ImcLin;
import lang24.phase.regall.RegAll;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PutItAllTogether {
    private final static String fileName = "out.mms";
    private final static String staticVariablesReplace = "{{static_variables}}";
    private final static String localSizeReplace = "{{local_size}}";
    private final static String framePlusTempSizeReplace = "{{frame_temp_size}}";
    private final static String staticVariablesSizeReplace = "{{static_size}}";
    private final static String bodyLabelReplace = "{{body_label}}";
    private final static String functionNameReplace = "{{function_name}}";
    private final static String labelToReplace = "{{label}}";
    private String functionName = "";


    public void start() {
        saveToAsmFile();
    }

    public void storeStaticData(FileWriter writer) throws IOException {
        List<String> staticVariables = new ArrayList<>();
        for(LinDataChunk data : ImcLin.dataChunks()) {
            String init = data.init == null ? "" : data.init;
            staticVariables.add(String.format("%s\t%s\t%s",data.label.name, "OCTA", init));
            staticVariables.add(String.format("\t\tLOC %s+%d",data.label.name, data.size));
        }

        String stringToAdd = String.join("\n", staticVariables);
        writer.write(getStaticPartFormatString(stringToAdd));
    }

    public void addMainFunction(FileWriter writer) throws IOException {
        int initialSize = 160;
        int size = ImcLin.dataChunks()
                .stream()
                .map(c -> (int)c.size)
                .mapToInt(Integer::intValue)
                .sum() + initialSize;

        writer.write(getMainFunctionFormatString(size));
    }

    public void saveToAsmFile() {
        try {
            File file = new File(fileName);
            if(file.exists()) {
                file.delete();
            }

            FileWriter writer = new FileWriter(fileName);

            storeStaticData(writer);
            addMainFunction(writer);

            for(Code code : AsmGen.codes) {
                functionName = code.frame.label.name;
                Report.info("PIAT: " + functionName);

                String instrString = getStringFromInstructions(code.instrs);

                addPrologue(code, writer);
                writer.write(instrString);
                addEpilogue(code, writer);
            }

            writer.write(stdLibString);
            writer.close();
        } catch (IOException ex) {
            throw new Report.Error("Saving to mms file exception: " + ex.getMessage());
        }
    }

    private void addPrologue(Code code, FileWriter writer) throws IOException {
        long localsSize = code.frame.locsSize;
        long frameSizeAndTempSize = code.frame.size + code.tempSize;
        String bodyLabel = code.entryLabel.name;
        writer.write(getPrologueFunctionFormatString(functionName, functionName, localsSize, frameSizeAndTempSize, bodyLabel));
    }

    private void addEpilogue(Code code, FileWriter writer) throws IOException {
        long localsSize = code.frame.locsSize;
        writer.write(getEpilogueFunctionFormatString(functionName, code.exitLabel.name, localsSize));
    }


    private String getStringFromInstructions(Vector<AsmInstr> instrs) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("%%%%%%%%%%%%%%%%% Body of");
        stringBuilder.append(functionName);
        stringBuilder.append(" %%%%%%%%%%%%%%%%%\n");

        for (var instr : instrs) {
            if(!(instr instanceof AsmOPER oper)) {
                Report.warning("Instr " + instr + " is not of type AsmOPER");
                continue;
            }

            if(instr instanceof AsmLABEL label) {
                stringBuilder.append(label);
                stringBuilder.append("\t\tSWYM\n");
            }
            else {
                String[] instrParts = oper.toString(RegAll.tempToReg).split(" ", 2);
                stringBuilder.append(String.format("\t%s\t%s\n", instrParts[0], instrParts[1]));
            }
        }

        stringBuilder.append("%%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%%\n\n");

        return stringBuilder.toString();
    }


    //todo finish that

    private String getStaticPartFormatString(String staticVariables) {
        String staticPartFormatString = """
            %%%%%%%%%%%%%%%%% STATIC %%%%%%%%%%%%%%%%%
                    LOC	Data_Segment
            OutBuf		BYTE	0,0
            InSize		IS	2
            InBuffer		BYTE	0
                    LOC	InBuffer+InSize
            InArgs		OCTA	InBuffer,InSize
            {{static_variables}}
            exit		BYTE	"EXIT CODE: ",0
                    LOC	exit+104
            code		BYTE	1,10,0
                    LOC	code+24
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
                
            """;

        return staticPartFormatString.replace(staticVariablesReplace, staticVariables);
    }

    private String getMainFunctionFormatString(long staticSize) {
        String mainFunctionFormatString = """
            %%%%%%%%%%%%%%%%% MAIN %%%%%%%%%%%%%%%%%%%
                    LOC	#100
            Main        SWYM
                    SET	$0,252
            %	Set rG as global registers threshold
                    PUT	rG,$0
                    GREG	0
                    GREG	0
                    GREG	0
            %	Set rL as local registers threshold
                    SET	$0,8
                    PUT	rL,$0
            %	Global register $252
            %	@Data_Segment (0x0000 0000 0000 0000 -> 0x0000 0007 1AFD 498D 0000)
                    SETL	$0,{{static_size}}
                    INCML	$0,0
                    INCMH	$0,0
                    INCH	$0,8192
                    SET	$0,$252
            %	Frame Pointer ($253), Stack Pointer($254)
            %	@Stack_Segment (0x0015 50F7 DCA7 0000 -> 0x7FFF FFFF FFFF FFF8)
                    SETL	$0,65528
                    INCML	$0,65535
                    INCMH	$0,65535
                    INCH	$0,32767
                    SET	$254,$0
            %	Call the main function
                    PUSHJ	$8,_main
            %	Print exit code
                    LDO	$0,$254,0
                    LDA	$255,exit
                    TRAP	0,Fputs,StdOut
                    LDA	$255,code
                    STB	$0,$255,0
                    TRAP	0,Fputs,StdOut
            %	Set return value
                    SET	$255,$0
            %	Exit the program
                    TRAP	0,Halt,0
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   \s
                
            """;

        return mainFunctionFormatString.replace(staticVariablesSizeReplace, Long.toString(staticSize));
    }

    private String getPrologueFunctionFormatString(String functionName, String label, long localSize, long frameTempSize, String bodyLabel) {
        String prologueFunctionFormatString =  """
            %%%%%%%%%%%%%%% Prologue of {{function_name}} %%%%%%%%%%%%%%%
            {{label}}		SWYM
            %	$0 <- local variables offset
                SETL	$0,{{local_size}}
                INCML	$0,0
                INCMH	$0,0
                INCH	$0,0
            %	Store RA
                NEG	$0,$0
                SUB	$0,$0,8
                GET	$1,rJ
                STO	$1,$254,$0
            %	Store FP
                SUB	$0,$0,8
                STO	$253,$254,$0
            %	Move FP
                SET	$253,$254
            %	Move SP ($0 <- frame size + temp size)
                SETL	$0,{{frame_temp_size}}
                INCML	$0,0
                INCMH	$0,0
                INCH	$0,0
                SUB	$254,$254,$0
            %	Jump to body
                JMP	{{body_label}}
                    \s
            """;
        prologueFunctionFormatString = prologueFunctionFormatString.replace(localSizeReplace, Long.toString(localSize));
        prologueFunctionFormatString = prologueFunctionFormatString.replace(framePlusTempSizeReplace, Long.toString(frameTempSize));
        prologueFunctionFormatString = prologueFunctionFormatString.replace(bodyLabelReplace, bodyLabel);
        prologueFunctionFormatString = prologueFunctionFormatString.replace(functionNameReplace, functionName);
        prologueFunctionFormatString = prologueFunctionFormatString.replace(labelToReplace, label);
        return prologueFunctionFormatString;
    }


    private String getEpilogueFunctionFormatString(String functionName, String label, long localSize) {
        String epilogueFunctionFormatString = """
            %%%%%%%%%%%%%%%%% Epilogue of {{function_name}} %%%%%%%%%%%%%%%%%
            {{label}}	    SWYM
            %	Store return value
                    STO	$0,$253,0
            %	SP <- FP
                    SET	$254,$253
            %	$0 <- local variables offset
                    SETL	$0,{{local_size}}
                    INCML	$0,0
                    INCMH	$0,0
                    INCH	$0,0
            %	Restore RA
                    NEG	$0,$0
                    SUB	$0,$0,8
                    ADD	$0,$253,$0
                    LDO	$1,$0,0
                    PUT	rJ,$1
            %	Restore FP
                    SUB	$0,$0,8
                    LDO	$253,$0,0
            %	Jump to RA
                    POP	0,0
            %%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%% %%%%%%%%%%%%%%%%%
            \s
            """;
        epilogueFunctionFormatString = epilogueFunctionFormatString.replace(localSizeReplace, Long.toString(localSize));
        epilogueFunctionFormatString = epilogueFunctionFormatString.replace(functionNameReplace, functionName);
        epilogueFunctionFormatString = epilogueFunctionFormatString.replace(labelToReplace, label);

        return epilogueFunctionFormatString;
    }

    private final String stdLibString = """
                
            %%%%%%%%%%%%%%%%% STANDARD LIBRARY %%%%%%%%%%%%%%%%%
            %	Print integer
            _putint  		SWYM
                    LDO	$0,$254,8
                    LDA	$255,OutBuf
                    STB	$0,$255,0
                    TRAP	0,Fputs,StdOut
                    POP
            %	Print character
            _putchar		SWYM
                    LDO	$0,$254,8
                    LDA	$255,OutBuf
                    STB	$0,$255,0
                    TRAP	0,Fputs,StdOut
                    POP
            
            %	Read character
            _getchar		SWYM
                    LDA	$255,InArgs
                    TRAP	0,Fgets,StdIn
                    LDA	$0,InBuffer
                    LDB	$0,$0,0
                    STO	$0,$254,0
                    POP
            %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
            \s
            """;

}
