package tgw.evolution.hooks.asm;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static org.objectweb.asm.Opcodes.*;

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

    default void handleConstructors(ClassNode original, String originalName, ClassNode mixin, String mixinName) {
        List<MethodNode> mixinMethods = mixin.methods;
        for (int i = 0, len = mixinMethods.size(); i < len; ++i) {
            MethodNode mixinMethod = mixinMethods.get(i);
            if ("<init>".equals(mixinMethod.name)) {
                List<AnnotationNode> annotations = mixinMethod.visibleAnnotations;
                if (annotations == null) {
                    continue;
                }
                a:
                for (int j = 0, len2 = annotations.size(); j < len2; ++j) {
                    AnnotationNode annotation = annotations.get(j);
                    if ("Ltgw/evolution/hooks/asm/ModifyConstructor;".equals(annotation.desc)) {
                        List<MethodNode> methods = original.methods;
                        for (int k = 0, len3 = methods.size(); k < len3; ++k) {
                            MethodNode method = methods.get(k);
                            if (this.findMethod(method, "<init>", mixinMethod.desc)) {
                                method.localVariables.clear();
                                method.tryCatchBlocks.clear();
                                this.patchMixin(method.instructions, originalName, mixinMethod.instructions, mixinName);
                                method.tryCatchBlocks.addAll(mixinMethod.tryCatchBlocks);
                                break a;
                            }
                        }
                        CoreModLoader.LOGGER.warn("Could not find constructor in the original class matching {}", mixinMethod.desc);
                    }
                }
            }
        }
    }

    default void handleFields(ClassNode original) {
        List<FieldNode> fields = original.fields;
        for (int i = 0; i < fields.size(); ++i) {
            FieldNode field = fields.get(i);
            List<AnnotationNode> annotations = field.visibleAnnotations;
            if (annotations == null) {
                continue;
            }
            boolean delete = false;
            int restoreFinal = -1;
            int finalIndex = -1;
            for (int j = 0, len = annotations.size(); j < len; ++j) {
                AnnotationNode annotation = annotations.get(j);
                if ("Ltgw/evolution/hooks/asm/DeleteField;".equals(annotation.desc)) {
                    delete = true;
                    break;
                }
                if (restoreFinal == -1 && "Ltgw/evolution/hooks/asm/RestoreFinal;".equals(annotation.desc)) {
                    restoreFinal = j;
                }
                else if ("Lorg/spongepowered/asm/mixin/Final;".equals(annotation.desc)) {
                    finalIndex = j;
                }
            }
            if (delete) {
                this.log("Deleted field " + field.name);
                fields.remove(i--);
                continue;
            }
            if (restoreFinal != -1) {
                field.access |= ACC_FINAL;
                this.log("Restored final on field " + field.name);
                if (finalIndex != -1) {
                    if (finalIndex > restoreFinal) {
                        annotations.remove(finalIndex);
                        annotations.remove(restoreFinal);
                    }
                    else {
                        annotations.remove(restoreFinal);
                        annotations.remove(finalIndex);
                    }
                }
                else {
                    annotations.remove(restoreFinal);
                }
                continue;
            }
            if (finalIndex != -1) {
                annotations.remove(finalIndex);
            }
        }
    }

    boolean handlesClass(String name, String mixinName);

    default void log(String message) {
        CoreModLoader.LOGGER.debug(this.name() + ": " + message);
    }

    default MethodInsnNode method(MethodReference method) {
        return new MethodInsnNode(method.opcode, method.owner, method.name, method.desc);
    }

    String name();

    default @Nullable AbstractInsnNode patchInst(AbstractInsnNode inst,
                                                 String originalOwner,
                                                 String originalParam,
                                                 String mixinOwner,
                                                 String mixinParam,
                                                 MutableBoolean b) {
        boolean wasLastThis = b.booleanValue();
        b.setFalse();
        return switch (inst.getOpcode()) {
            case ANEWARRAY, CHECKCAST, INSTANCEOF, NEW -> {
                TypeInsnNode type = (TypeInsnNode) inst;
                if (type.desc.equals(mixinOwner)) {
                    if (inst.getOpcode() == CHECKCAST) {
                        yield null;
                    }
                    type.desc = originalOwner;
                }
                else if (wasLastThis && inst.getOpcode() == CHECKCAST && type.desc.equals(originalOwner)) {
                    yield null;
                }
                yield type;
            }
            case GETSTATIC, PUTSTATIC, GETFIELD, PUTFIELD -> {
                FieldInsnNode field = (FieldInsnNode) inst;
                if (field.owner.equals(mixinOwner)) {
                    field.owner = originalOwner;
                }
                if (field.desc.equals(mixinParam)) {
                    field.desc = originalParam;
                }
                yield field;
            }
            case INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE -> {
                MethodInsnNode method = (MethodInsnNode) inst;
                if (method.owner.equals(mixinOwner)) {
                    method.owner = originalOwner;
                }
                method.desc = method.desc.replace(mixinParam, originalParam);
                yield method;
            }
            case INVOKEDYNAMIC -> {
                InvokeDynamicInsnNode dynamic = (InvokeDynamicInsnNode) inst;
                Object[] args = dynamic.bsmArgs;
                for (int i = 0, len = args.length; i < len; i++) {
                    if (args[i] instanceof Handle handle) {
                        if (mixinOwner.equals(handle.getOwner())) {
                            args[i] = new Handle(handle.getTag(), originalOwner, handle.getName(), handle.getDesc(), handle.isInterface());
                        }
                    }
                }
                dynamic.desc = dynamic.desc.replace(mixinParam, originalParam);
                yield dynamic;
            }
            case ALOAD -> {
                VarInsnNode var = (VarInsnNode) inst;
                if (var.var == 0) {
                    b.setTrue();
                }
                yield inst;
            }
            default -> inst;
        };
    }

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

    default void patchMixin(InsnList original, String originalName, InsnList mixin, String mixinName) {
        String mixinOwner = mixinName.replace('.', '/');
        String mixinParam = "L" + mixinOwner + ";";
        String originalOwner = originalName.replace('.', '/');
        String originalParam = "L" + originalOwner + ";";
        original.clear();
        MutableBoolean wasLastThis = new MutableBoolean(false);
        for (int i = 0, len = mixin.size(); i < len; ++i) {
            AbstractInsnNode inst = mixin.get(i);
            AbstractInsnNode patched = this.patchInst(inst, originalOwner, originalParam, mixinOwner, mixinParam, wasLastThis);
            if (patched != null) {
                original.add(patched);
            }
        }
    }

    default FieldInsnNode putField(FieldReference field) {
        return new FieldInsnNode(PUTFIELD, field.owner, field.name, field.desc);
    }

    default @Nullable ClassNode readMixinClass(String mixinClassName) {
        try {
            ClassReader reader = new ClassReader(mixinClassName);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.SKIP_FRAMES);
            return node;
        }
        catch (IOException e) {
            CoreModLoader.LOGGER.error("Could not read Mixin Class: {}", mixinClassName);
            CoreModLoader.LOGGER.error("Exception:", e);
            return null;
        }
    }

    boolean shouldRemoveAfterPatch();

    void transformClass(ClassNode classNode, String className, String mixinClassName, IMixinInfo info);
}
