package tgw.evolution.util.reflection;

public class FunctionMethodHandler<Owner, Type, Arg> extends MethodHandler<Owner, Type> {

    public FunctionMethodHandler(Class<Owner> methodClazz, String methodName, Class<Arg> parameterClazz) {
        super(methodClazz, methodName, parameterClazz);
    }

    public Type call(Owner owner, Arg arg) {
        return super.call(owner, arg);
    }
}
