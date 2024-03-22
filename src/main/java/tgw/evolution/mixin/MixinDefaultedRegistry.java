package tgw.evolution.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchDefaultedRegistry;

import java.util.Optional;
import java.util.function.Function;

@Mixin(DefaultedRegistry.class)
public abstract class MixinDefaultedRegistry<T> extends MappedRegistry<T> implements PatchDefaultedRegistry<T> {

    public MixinDefaultedRegistry(ResourceKey<? extends Registry<T>> resourceKey, Lifecycle lifecycle, @Nullable Function<T, Holder.Reference<T>> function) {
        super(resourceKey, lifecycle, function);
    }

    @Override
    public @Nullable T getNullable(@Nullable ResourceLocation resLoc) {
        return super.get(resLoc);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public Optional<T> getOptional(@Nullable ResourceLocation resLoc) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(this.getNullable(resLoc));
    }
}
