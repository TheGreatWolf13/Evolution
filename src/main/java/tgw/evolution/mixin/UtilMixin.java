package tgw.evolution.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Util.class)
public abstract class UtilMixin {

    /**
     * @author TheGreatWolf
     * @reason Remove Datafixers.
     */
    @javax.annotation.Nullable
    @Overwrite
    @Nullable
    private static Type<?> doFetchChoiceType(DSL.TypeReference pType, String pChoiceName) {
        return null;
    }
}
