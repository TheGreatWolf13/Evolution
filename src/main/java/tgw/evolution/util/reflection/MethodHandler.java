package tgw.evolution.util.reflection;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodHandler<Owner, Type> implements IReflectionHandler {

    private final String methodName;
    private final Class<Owner> methodOwner;
    private final Class<?>[] parameterTypes;

    private Method methodAccess;

    public MethodHandler(Class<Owner> methodClass, String methodName, Class<?>... parameterTypes) {
        this.methodName = methodName;
        this.methodOwner = methodClass;
        this.parameterTypes = parameterTypes.clone();
    }

    @Nullable
    public final Type call(Owner methodInstance, Object... methodParams) {
        this.init();
        try {
            return (Type) this.methodAccess.invoke(methodInstance, methodParams);
        }
        catch (final Exception exception) {
            IReflectionHandler.throwError(exception,
                                          "Error invoking method: " +
                                          this.methodOwner.getName() +
                                          '#' +
                                          this.methodName +
                                          '(' +
                                          Arrays.toString(this.parameterTypes) +
                                          ')');
        }
        return null;
    }

    @Override
    public void init() {
        if (null == this.methodAccess) {
            try {
                this.methodAccess = ObfuscationReflectionHelper.findMethod(this.methodOwner, this.methodName, this.parameterTypes);
            }
            catch (Exception exception) {
                IReflectionHandler.throwError(exception,
                                              "Error finding method: " +
                                              this.methodOwner.getName() +
                                              '#' +
                                              this.methodName +
                                              '(' +
                                              Arrays.toString(this.parameterTypes) +
                                              ')');
            }
        }
    }
}
