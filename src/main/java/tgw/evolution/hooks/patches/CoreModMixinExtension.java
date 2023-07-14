package tgw.evolution.hooks.patches;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import tgw.evolution.hooks.asm.CoreModLoader;
import tgw.evolution.hooks.asm.EvolutionHook;
import tgw.evolution.hooks.asm.IClassTransformer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EvolutionHook
public class CoreModMixinExtension implements IClassTransformer {

    private Method method;

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
    public void transformClass(ClassNode classNode,
                               String className,
                               String mixinClassName,
                               IMixinInfo info) {
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
        if (modifiers.indexOf('F') >= 0) {
            this.handleFields(classNode);
        }
    }
}
