package tgw.evolution.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.patches.obj.DummyDataFixerBuilder;

@Mixin(DataFixers.class)
public abstract class DataFixersMixin {

    /**
     * @author TheGreatWolf
     * @reason Remove datafixers, as it doesn't make any sense to waste memory with that in this modded environment
     */
    @Overwrite
    private static DataFixer createFixerUpper() {
        return new DummyDataFixerBuilder(SharedConstants.getCurrentVersion().getWorldVersion()).build(Util.bootstrapExecutor());
    }
}
