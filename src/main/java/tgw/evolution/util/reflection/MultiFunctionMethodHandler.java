package tgw.evolution.util.reflection;

import javax.annotation.Nullable;

public class MultiFunctionMethodHandler<Owner, Type> extends MethodHandler<Owner, Type> {

    public MultiFunctionMethodHandler(Class<Owner> methodClass, String methodName, Class<?>... parameterTypes) {
        super(methodClass, methodName, parameterTypes);
    }

    @Nullable
    @Override
    public Type call(Owner methodInstance, Object... methodParams) {
        return super.call(methodInstance, methodParams);
    }
}
