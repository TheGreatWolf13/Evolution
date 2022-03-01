package tgw.evolution.util.reflection;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class StaticFieldHandler<Owner, Type> implements IReflectionHandler {

    private final String fieldName;
    private final Class<Owner> fieldOwner;
    private Field fieldAccess;

    public StaticFieldHandler(Class<Owner> fieldOwner, String fieldName) {
        this.fieldName = fieldName;
        this.fieldOwner = fieldOwner;
    }

    /**
     * Get this field value
     *
     * @return The value, or null if an exception was thrown.
     */
    @Nullable
    public final Type get() {
        this.init();
        try {
            return (Type) this.fieldAccess.get(null);
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not get field: " + this.fieldOwner.getName() + '#' + this.fieldName);
        }
        return null;
    }

    @Override
    public void init() {
        if (null == this.fieldAccess) {
            try {
                this.fieldAccess = ObfuscationReflectionHelper.findField(this.fieldOwner, this.fieldName);
            }
            catch (final RuntimeException exception) {
                IReflectionHandler.throwError(exception, "Could not find field: " + this.fieldOwner.getName() + '#' + this.fieldName);
            }
        }
    }

    public final void set(Type type) {
        this.init();
        try {
            if ((this.fieldAccess.getModifiers() & Modifier.FINAL) != 0) {
                IReflectionHandler.throwError(new IllegalAccessException(),
                                              "Cannot set the FINAL field: " + this.fieldOwner.getName() + '#' + this.fieldName);
            }
            else {
                this.fieldAccess.set(null, type);
            }
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not set field: " + this.fieldOwner.getName() + '#' + this.fieldName);
        }
    }
}
