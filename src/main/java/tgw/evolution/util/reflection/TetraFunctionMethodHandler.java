package tgw.evolution.util.reflection;

public class TetraFunctionMethodHandler<Owner, Type, Arg1, Arg2, Arg3, Arg4> extends MethodHandler<Owner, Type> {

    public TetraFunctionMethodHandler(Class<Owner> methodClass,
                                      String methodName,
                                      Class<Arg1> par1,
                                      Class<Arg2> par2,
                                      Class<Arg3> par3,
                                      Class<Arg4> par4) {
        super(methodClass, methodName, par1, par2, par3, par4);
    }

    public Type call(Owner owner, Arg1 arg1, Arg2 arg2, Arg3 arg3, Arg4 arg4) {
        return super.call(owner, arg1, arg2, arg3, arg4);
    }
}
