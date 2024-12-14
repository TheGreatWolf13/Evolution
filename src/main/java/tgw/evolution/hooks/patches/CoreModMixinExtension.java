package tgw.evolution.hooks.patches;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import tgw.evolution.hooks.asm.CoreModLoader;
import tgw.evolution.hooks.asm.EvolutionHook;
import tgw.evolution.hooks.asm.IClassTransformer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

@EvolutionHook
public class CoreModMixinExtension implements IClassTransformer {

    private @Nullable Method method;

    private static @Nullable AbstractInsnNode patchInst(AbstractInsnNode inst, String originalOwner, String originalParam, String mixinOwner, String mixinParam, MutableBoolean b) {
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
                else if (type.desc.startsWith(mixinOwner)) {
                    type.desc = type.desc.replace(mixinOwner, originalOwner);
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
                else if (method.owner.startsWith(mixinOwner)) {
                    method.owner = method.owner.replace(mixinOwner, originalOwner);
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

    private static void patchMixin(InsnList original, String originalName, InsnList mixin, String mixinName) {
        String mixinOwner = mixinName.replace('.', '/');
        String mixinParam = "L" + mixinOwner + ";";
        String originalOwner = originalName.replace('.', '/');
        String originalParam = "L" + originalOwner + ";";
        original.clear();
        MutableBoolean wasLastThis = new MutableBoolean(false);
        for (int i = 0, len = mixin.size(); i < len; ++i) {
            AbstractInsnNode inst = mixin.get(i);
            AbstractInsnNode patched = patchInst(inst, originalOwner, originalParam, mixinOwner, mixinParam, wasLastThis);
            if (patched != null) {
                original.add(patched);
            }
        }
    }

    private void handleConstructors(ClassNode original, String originalName, ClassNode mixin, String mixinName) {
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
                                patchMixin(method.instructions, originalName, mixinMethod.instructions, mixinName);
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

    private void handleFields(ClassNode original) {
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

    private void handleMethods(ClassNode original) {
        List<MethodNode> methods = original.methods;
        for (int i = 0; i < methods.size(); ++i) {
            MethodNode method = methods.get(i);
            List<AnnotationNode> annotations = method.visibleAnnotations;
            if (annotations == null) {
                continue;
            }
            boolean delete = false;
            for (int j = 0, len = annotations.size(); j < len; ++j) {
                AnnotationNode annotation = annotations.get(j);
                if ("Ltgw/evolution/hooks/asm/DeleteMethod;".equals(annotation.desc)) {
                    delete = true;
                    break;
                }
            }
            if (delete) {
                //noinspection ObjectAllocationInLoop
                this.log("Deleted method " + method.name);
                methods.remove(i--);
            }
        }
    }

    private void handleStatic(ClassNode original, String originalName, String mixinName) {
        List<MethodNode> methods = original.methods;
        for (int i = 0, len = methods.size(); i < len; ++i) {
            MethodNode methodTemplate = methods.get(i);
            List<AnnotationNode> annotations = methodTemplate.visibleAnnotations;
            if (annotations == null) {
                continue;
            }
            for (int j = 0, len2 = annotations.size(); j < len2; ++j) {
                if ("Ltgw/evolution/hooks/asm/ModifyStatic;".equals(annotations.get(j).desc)) {
                    if (!"()V".equals(methodTemplate.desc)) {
                        CoreModLoader.LOGGER.error("Invalid signature for ModifyStatic: {}", methodTemplate.desc);
                        return;
                    }
                    if ((methodTemplate.access & ACC_STATIC) == 0) {
                        CoreModLoader.LOGGER.error("Method for ModifyStatic should be static");
                        return;
                    }
                    for (int k = 0, len3 = methods.size(); k < len3; ++k) {
                        MethodNode method = methods.get(k);
                        if (this.findMethod(method, "<clinit>", methodTemplate.desc)) {
                            method.localVariables.clear();
                            method.tryCatchBlocks.clear();
                            patchMixin(method.instructions, originalName, methodTemplate.instructions, mixinName);
                            method.tryCatchBlocks.addAll(methodTemplate.tryCatchBlocks);
                            methods.remove(i);
                            return;
                        }
                    }
                    CoreModLoader.LOGGER.warn("Could not find static initializer in the original class");
                }
            }
        }
    }

    @Override
    public boolean handlesClass(String name, String mixinName) {
        int index = mixinName.lastIndexOf('.');
        return mixinName.charAt(index + 1) == 'M' &&
               mixinName.charAt(index + 2) == 'i' &&
               mixinName.charAt(index + 3) == 'x' &&
               mixinName.charAt(index + 4) == 'i' &&
               mixinName.charAt(index + 5) == 'n' &&
               mixinName.charAt(index + 6) == '_';
    }

    @Override
    public String name() {
        return "MixinExtension";
    }

    private @Nullable ClassNode readMixin(IMixinInfo info, String mixinName) {
        if (this.method == null) {
            try {
                Class<?> clazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
                Method loadMixinClass = clazz.getDeclaredMethod("loadMixinClass", String.class);
                loadMixinClass.setAccessible(true);
                this.method = loadMixinClass;
            }
            catch (ClassNotFoundException e) {
                CoreModLoader.LOGGER.error("Could not locate MixinInfo class!", e);
                return null;
            }
            catch (NoSuchMethodException e) {
                CoreModLoader.LOGGER.error("Could not locate loadMixinClass method!", e);
                return null;
            }
        }
        try {
            return (ClassNode) this.method.invoke(info, mixinName);
        }
        catch (InvocationTargetException e) {
            CoreModLoader.LOGGER.error("loadMixinClass threw an exception!", e);
            return null;
        }
        catch (IllegalAccessException e) {
            CoreModLoader.LOGGER.error("loadMixinClass is inaccessible", e);
            return null;
        }
    }

    @Override
    public boolean shouldRemoveAfterPatch() {
        return false;
    }

    @Override
    public void transformClass(ClassNode classNode, String className, String mixinClassName, IMixinInfo info) {
        this.log("Patching class: " + className + " from " + mixinClassName);
        int index = mixinClassName.lastIndexOf('.');
        assert mixinClassName.charAt(index + 6) == '_';
        String modifiers = mixinClassName.substring(index + 7, mixinClassName.indexOf('_', index + 7));
        if (modifiers.indexOf('C') >= 0) {
            ClassNode mixinNode = this.readMixin(info, mixinClassName);
            if (mixinNode == null) {
                return;
            }
            this.handleConstructors(classNode, className, mixinNode, mixinClassName);
        }
        if (modifiers.indexOf('S') >= 0) {
            this.handleStatic(classNode, className, mixinClassName);
        }
        if (modifiers.indexOf('F') >= 0) {
            this.handleFields(classNode);
        }
        if (modifiers.indexOf('M') >= 0) {
            this.handleMethods(classNode);
        }
    }
}
