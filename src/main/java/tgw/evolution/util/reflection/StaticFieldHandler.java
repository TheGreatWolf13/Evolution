package tgw.evolution.util.reflection;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class StaticFieldHandler<Owner, Type> implements IReflectionHandler {

    private final Class<Owner> fieldOwner;
    private final String fieldName;
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

    /**
     * Set this field to specified value
     *
     * @param fieldValue The value to set
     */
    public final void set(Type fieldValue) {
        this.init();
        try {
            this.fieldAccess.set(null, fieldValue);
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not set field: " + this.fieldOwner.getName() + '#' + this.fieldName);
        }
    }
}
