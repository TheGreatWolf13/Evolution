package tgw.evolution.hooks.asm;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.PUTFIELD;

/**
 * Classes extending should have a default, zero-arg constructor
 */
public interface IClassTransformer {

    default boolean findMethod(MethodNode method,
                               String name,
                               @Nullable String desc) {
        if (!method.name.equals(name)) {
            return false;
        }
        if (desc != null && !method.desc.equals(desc)) {
            return false;
        }
        this.log("Patching method: " + name + method.desc);
        return true;
    }

    boolean handlesClass(String name, String mixinName);

    default void log(String message) {
        CoreModLoader.LOGGER.debug(this.name() + ": " + message);
    }

    default MethodInsnNode method(MethodReference method) {
        return new MethodInsnNode(method.opcode, method.owner, method.name, method.desc);
    }

    String name();

    default boolean patchMethod(MethodNode method,
                                String name,
                                @Nullable String desc,
                                Consumer<InsnList> patcher) {
        if (!method.name.equals(name)) {
            return false;
        }
        if (desc != null && !method.desc.equals(desc)) {
            return false;
        }
        this.log("Patching method: " + name + method.desc);
        patcher.accept(method.instructions);
        return true;
    }

    default FieldInsnNode putField(FieldReference field) {
        return new FieldInsnNode(PUTFIELD, field.owner, field.name, field.desc);
    }

    boolean shouldRemoveAfterPatch();

    void transformClass(ClassNode classNode, String className, String mixinClassName, IMixinInfo info);
}
