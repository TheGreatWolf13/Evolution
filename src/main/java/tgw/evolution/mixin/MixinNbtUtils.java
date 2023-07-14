package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.datafix.DataFixTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(NbtUtils.class)
public abstract class MixinNbtUtils {

    /**
     * @author TheGreatWolf
     * @reason Remove DataFixers references, as they are disabled.
     */
    @Overwrite
    public static CompoundTag update(DataFixer dataFixer, DataFixTypes type, CompoundTag tag, int version, int newVersion) {
        return tag;
    }

    /**
     * @author TheGreatWolf
     * @reason Remove DataFixers references, as they are disabled.
     */
    @Overwrite
    public static CompoundTag update(DataFixer dataFixer, DataFixTypes type, CompoundTag tag, int version) {
        return tag;
    }
}
