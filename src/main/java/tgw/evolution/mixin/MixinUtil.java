package tgw.evolution.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.types.Type;
import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(Util.class)
public abstract class MixinUtil {

    /**
     * @author TheGreatWolf
     * @reason Remove DataFixers.
     */
    @Overwrite
    private static @Nullable Type<?> doFetchChoiceType(DSL.TypeReference pType, String pChoiceName) {
        return null;
    }
}
