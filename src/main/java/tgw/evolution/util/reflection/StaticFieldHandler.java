package tgw.evolution.util.reflection;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class StaticFieldHandler<Owner, Type> implements IReflectionHandler {

    private static final Field MODIFIERS;

    static {
        Field modifiers;
        try {
            modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            modifiers = null;
            IReflectionHandler.throwError(e, "Could not initialize StaticFieldHandler changing capabilities!");
        }
        MODIFIERS = modifiers;
    }

    private final String fieldName;
    private final Class<Owner> fieldOwner;
    private final boolean needsChanging;
    private Field fieldAccess;

    public StaticFieldHandler(Class<Owner> fieldOwner, String fieldName) {
        this(fieldOwner, fieldName, false);
    }

    public StaticFieldHandler(Class<Owner> fieldOwner, String fieldName, boolean needsChanging) {
        this.fieldName = fieldName;
        this.fieldOwner = fieldOwner;
        this.needsChanging = needsChanging;
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
                if (this.needsChanging && MODIFIERS != null) {
                    MODIFIERS.setInt(this.fieldAccess, this.fieldAccess.getModifiers() & ~Modifier.FINAL);
                }
            }
            catch (final RuntimeException | IllegalAccessException exception) {
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
        if (!this.needsChanging) {
            IReflectionHandler.throwError(new IllegalAccessException("Handler not set to allow changing!"),
                                          "Could not set field: " + this.fieldOwner.getName() + '#' + this.fieldName);
            return;
        }
        this.init();
        try {
            this.fieldAccess.set(null, fieldValue);
        }
        catch (IllegalAccessException exception) {
            IReflectionHandler.throwError(exception, "Could not set field: " + this.fieldOwner.getName() + '#' + this.fieldName);
        }
    }
}
