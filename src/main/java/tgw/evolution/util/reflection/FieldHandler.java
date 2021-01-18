package tgw.evolution.util.reflection;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

public class FieldHandler<Owner, Type> implements IReflectionHandler {

    private final String fieldName;
    private final Class<Owner> fieldOwner;
    private Field fieldAccess;

    public FieldHandler(Class<Owner> fieldOwner, String fieldName) {
        this.fieldName = fieldName;
        this.fieldOwner = fieldOwner;
    }

    /**
     * Get this field value
     *
     * @param fieldInstance The object instance that owns this field
     * @return The value, or null if an exception was thrown.
     */
    @Nullable
    public final Type get(Owner fieldInstance) {
        this.init();
        if (fieldInstance == null) {
            IReflectionHandler.throwError(new NullPointerException(),
                                          "Trying to get field on null instance: " + this.fieldOwner.getName() + '#' + this.fieldName);
            return null;
        }
        try {
            return (Type) this.fieldAccess.get(fieldInstance);
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not get field: " + fieldInstance.getClass().getName() + '#' + this.fieldName);
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
     * @param fieldOwner The object instance that owns this field
     * @param fieldValue The value to set
     */
    public final void set(Owner fieldOwner, Type fieldValue) {
        this.init();
        if (fieldOwner == null) {
            IReflectionHandler.throwError(new NullPointerException(),
                                          "Trying to set field on null instance: " + this.fieldOwner.getName() + '#' + this.fieldName);
            return;
        }
        try {
            this.fieldAccess.set(fieldOwner, fieldValue);
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not set field: " + this.fieldOwner.getName() + '#' + this.fieldName);
        }
    }
}
