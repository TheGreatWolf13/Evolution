package tgw.evolution.patches;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface PatchDefaultedRegistry<T> {

    default @Nullable T getNullable(@Nullable ResourceLocation resLoc) {
        throw new AbstractMethodError();
    }
}
