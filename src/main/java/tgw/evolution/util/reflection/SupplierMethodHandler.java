package tgw.evolution.util.reflection;

import javax.annotation.Nullable;

public class SupplierMethodHandler<Owner, Type> extends MethodHandler<Owner, Type> {

    public SupplierMethodHandler(Class<Owner> methodClass, String methodName) {
        super(methodClass, methodName);
    }

    @Nullable
    @Override
    public Type call(Owner methodInstance) {
        return super.call(methodInstance);
    }
}
