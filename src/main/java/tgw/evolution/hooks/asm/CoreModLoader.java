package tgw.evolution.hooks.asm;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

public class CoreModLoader implements IMixinConfigPlugin {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean DEBUG = false;

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public @Nullable List<String> getMixins() {
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            ModMetadata metadata = mod.getMetadata();
            if (metadata.containsCustomValue("coremods")) {
                for (CustomValue value : metadata.getCustomValue("coremods").getAsArray()) {
                    try {
                        Constructor<? extends IClassTransformer> transformer = Class.forName(value.getAsString())
                                                                                    .asSubclass(IClassTransformer.class)
                                                                                    .getDeclaredConstructor();
                        transformer.setAccessible(true);
                        Transformers.registerTransformer(transformer.newInstance());
                    }
                    catch (ReflectiveOperationException e) {
                        throw new RuntimeException(String.format("Failed to load transformer %s from %s", value.getAsString(), metadata.getId()), e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable String getRefMapperConfig() {
        return "evolution-refmap.json";
    }

    @Override
    public void onLoad(String mixinPackage) {
        LOGGER.info("CoreModLoader loaded!");
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {
        Transformers.transform(targetClassName, targetClass, mixinClassName, info);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }
}
