package tgw.evolution.hooks.asm;

public class MDBI extends MDB {

    private final String returnValueInt;
    private String methodInt = "(";

    public MDBI(String returnValue) {
        super(returnValue);
        this.returnValueInt = returnValue;
    }

    public MDBI(Class<?> returnValue) {
        super(returnValue);
        this.returnValueInt = "L" + returnValue.getName().replace('.', '/') + ";";
    }

    public MDBI(ClassReference returnValue) {
        super(returnValue);
        this.returnValueInt = returnValue.paramInt;
    }

    public String desc() {
        return this.method;
    }

    public String descInt() {
        return this.methodInt;
    }

    @Override
    public String end() {
        throw new UnsupportedOperationException();
    }

    public MDBI finish() {
        this.methodInt += ")" + this.returnValueInt;
        super.end();
        return this;
    }

    @Override
    public MDBI of(String param) {
        this.methodInt += param;
        super.of(param);
        return this;
    }

    @Override
    public MDBI of(ClassReference param) {
        this.methodInt += param.paramInt;
        super.of(param);
        return this;
    }

    @Override
    public MDBI of(Class<?> param) {
        this.methodInt += "L" + param.getName().replace('.', '/') + ";";
        super.of(param);
        return this;
    }
}
