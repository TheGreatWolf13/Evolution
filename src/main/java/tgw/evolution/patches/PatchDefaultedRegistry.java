package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface PatchDefaultedRegistry<T> {

    default @Nullable T getNullable(@Nullable ResourceLocation resLoc) {
        throw new AbstractMethodError();
    }

    default Optional<T> getOptional_(@Nullable ResourceLocation resLoc) {
        throw new AbstractMethodError();
    }
}
