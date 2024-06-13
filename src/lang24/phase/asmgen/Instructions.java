package lang24.phase.asmgen;

public class Instructions {

    /* Static Pointers */

    public static String FP = "$253";
    public static String SP = "$254";

    /*Memory instructions*/
    public static String STO = "STO";
    public static String LDO = "LDO";
    public static String LDA = "LDA";
    public static String SETH = "SETH";
    public static String SETMH = "SETMH";
    public static String SETML = "SETML";
    public static String SETL = "SETL";
    public static String INCML = "INCML";
    public static String INCMH = "INCMH";
    public static String INCH = "INCH";

    /* Arithmetic instructions */
    public static String ADD = "ADD";
    public static String SUB = "SUB";
    public static String MUL = "MUL";
    public static String DIV = "DIV";

    /* Bitwise and boolean instructions */
    public static String AND = "AND";
    public static String OR = "OR";

    /* Conditionals */
    public static String CMP = "CMP";
    public static String ZSZ = "ZSZ";
    public static String ZSP = "ZSP";
    public static String ZSN = "ZSN";
    public static String ZSNP = "ZSNP";
    public static String ZSNN = "ZSNN";
    public static String BP = "BP";


    /* Function instructions */
    public static String PUSHJ = "PUSHJ";
    public static String POP = "POP";

    /* Other */
    public static String JMP = "JMP";
}
