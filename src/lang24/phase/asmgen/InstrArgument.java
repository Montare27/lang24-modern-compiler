package lang24.phase.asmgen;

import lang24.data.mem.MemLabel;
import lang24.data.mem.MemTemp;

public class InstrArgument {
    public Type type = Type.None;
    private MemTemp valueTemp;
    private MemLabel valueLabel;
    private String valueString;
    private int id;

    public InstrArgument(MemTemp valueTemp, Type type) {
        this.type = type;
        this.valueTemp = valueTemp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public InstrArgument(MemTemp valueTemp) {
        this(valueTemp, Type.Use);
    }

    public InstrArgument(String valueString) {
        this(null, Type.None);
        this.valueString = valueString;
    }

    public InstrArgument(MemLabel label) {
        this(null, Type.Label);
        this.valueLabel = label;
    }

    public boolean isMemTemp() {return valueTemp != null; }

    public InstrArgument(Object value) {
        if(value instanceof MemTemp) {
            this.valueTemp = (MemTemp) value;
            type = Type.Use;
        } else if(value instanceof MemLabel) {
            this.valueLabel = (MemLabel) value;
            type = Type.Label;
        } else {
          this.valueString = value.toString();
        }
    }

    public Object getValue() {
        return switch (type) {
            case Label -> valueLabel;
            case None -> valueString;
            default -> valueTemp;
        };
    }

    public enum Type {
        Defn,
        Use,
        Label,
        None
    }

    @Override
    public String toString() {
        if(isMemTemp()) {

            if(valueTemp.equals(CodeGenerator.FP))
                return Instructions.FP;

            return "`" + (type == Type.Use ? "s" : "d") + id;
        }

        return type == Type.Label
                ? valueLabel.name
                : valueString;
    }
}
