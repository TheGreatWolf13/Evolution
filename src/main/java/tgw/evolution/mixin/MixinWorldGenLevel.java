package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchWorldGenLevel;

@Mixin(WorldGenLevel.class)
public interface MixinWorldGenLevel extends ServerLevelAccessor, PatchWorldGenLevel {
    
    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    default boolean ensureCanWrite(BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.ensureCanWrite_(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    default boolean ensureCanWrite_(int x, int y, int z) {
        return true;
    }
}
