package tgw.evolution.mixin;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryResourceAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RegistryAccess.class)
public interface MixinRegistryAccess {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private static <E> void addBuiltinElements(RegistryResourceAccess.InMemoryStorage memoryStorage, RegistryAccess.RegistryData<E> registryData) {
        Registry<E> registry = BuiltinRegistries.ACCESS.registryOrThrow(registryData.key());
        for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
            E e = (E) registry.getIteration(it);
            memoryStorage.add(BuiltinRegistries.ACCESS, registry.getIterationKey(it), registryData.codec(), registry.getId(e), e, registry.lifecycle(e));
        }
    }
}
