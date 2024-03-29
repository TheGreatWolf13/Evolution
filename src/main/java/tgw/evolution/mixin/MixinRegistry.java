package tgw.evolution.mixin;

import com.mojang.serialization.Keyable;
import net.minecraft.Util;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Registry.class)
public abstract class MixinRegistry<T> implements Keyable, IdMap<T> {

    @Shadow @Final public static Registry<? extends Registry<?>> REGISTRY;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static <T extends Registry<?>> void checkRegistry(Registry<T> registry) {
        for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
            Registry<?> registry2 = (Registry<?>) registry.getIteration(it);
            if (registry2.size() == 0) {
                //noinspection ObjectAllocationInLoop
                Util.logAndPauseIfInIde("Registry '" + registry.getKey((T) registry2) + "' was empty after loading");
            }
            if (registry2 instanceof DefaultedRegistry r) {
                ResourceLocation resourceLocation = r.getDefaultKey();
                //noinspection ObjectAllocationInLoop
                Validate.notNull(registry2.get(resourceLocation), "Missing default of DefaultedMappedRegistry: " + resourceLocation);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void freezeBuiltins() {
        for (long it = REGISTRY.beginIteration(); REGISTRY.hasNextIteration(it); it = REGISTRY.nextEntry(it)) {
            ((Registry<?>) REGISTRY.getIteration(it)).freeze();
        }
    }
}
