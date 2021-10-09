package tgw.evolution.util.reflection;

public class BiFunctionMethodHandler<Owner, Type, Arg1, Arg2> extends MethodHandler<Owner, Type> {

    public BiFunctionMethodHandler(Class<Owner> methodClass, String methodName, Class<Arg1> par1, Class<Arg2> par2) {
        super(methodClass, methodName, par1, par2);
    }

    public Type call(Owner owner, Arg1 arg1, Arg2 arg2) {
        return super.call(owner, arg1, arg2);
    }
}
