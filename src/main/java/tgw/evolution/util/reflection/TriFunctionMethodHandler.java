package tgw.evolution.util.reflection;

public class TriFunctionMethodHandler<Owner, Type, Arg1, Arg2, Arg3> extends MethodHandler<Owner, Type> {

    public TriFunctionMethodHandler(Class<Owner> methodClass, String methodName, Class<Arg1> par1, Class<Arg2> par2, Class<Arg3> par3) {
        super(methodClass, methodName, par1, par2, par3);
    }

    public Type call(Owner owner, Arg1 arg1, Arg2 arg2, Arg3 arg3) {
        return super.call(owner, arg1, arg2, arg3);
    }
}
