package tgw.evolution.hooks.asm;

/**
 * Stands for Method Description Builder.
 */
public class MDB {

    private final String returnValue;
    protected String method = "(";

    public MDB(String returnValue) {
        this.returnValue = returnValue;
    }

    public MDB(Class<?> returnValue) {
        this("L" + returnValue.getName().replace('.', '/') + ";");
    }

    public MDB(ClassReference returnValue) {
        this(returnValue.param);
    }

    public String end() {
        this.method += ")" + this.returnValue;
        return this.method;
    }

    public MDB of(Class<?> param) {
        this.method += "L" + param.getName().replace('.', '/') + ";";
        return this;
    }

    public MDB of(ClassReference param) {
        this.method += param.param;
        return this;
    }

    public MDB of(String param) {
        this.method += param;
        return this;
    }
}
