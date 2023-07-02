package tgw.evolution.mixin;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings({"EqualsAndHashcode", "NonFinalFieldReferencedInHashCode"})
@Mixin(targets = "net.minecraftforge.registries.RegistryDelegate")
public abstract class RegistryDelegateMixin {

    @Shadow
    private @Nullable ResourceLocation name;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Override
    @Overwrite
    public int hashCode() {
        return this.name != null ? this.name.hashCode() : 0;
    }
}
