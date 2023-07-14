package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.PlayerRespawnLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PlayerRespawnLogic.class)
public abstract class MixinPlayerRespawnLogic {

    /**
     * @author TheGreatWolf
     * @reason Use non-BlockPos versions
     */
    @Overwrite
    public static @Nullable BlockPos getOverworldRespawnPos(ServerLevel level, int x, int z) {
        boolean ceiling = level.dimensionType().hasCeiling();
        LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
        int height = ceiling ?
                     level.getChunkSource().getGenerator().getSpawnHeight(level) :
                     chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x & 15, z & 15);
        if (height < level.getMinBuildHeight()) {
            return null;
        }
        int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x & 15, z & 15);
        if (surface <= height && surface > chunk.getHeight(Heightmap.Types.OCEAN_FLOOR, x & 15, z & 15)) {
            return null;
        }
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int y = height + 1; y >= level.getMinBuildHeight(); --y) {
            BlockState state = level.getBlockState_(x, y, z);
            if (!state.getFluidState().isEmpty()) {
                break;
            }
            if (Block.isFaceFull(state.getCollisionShape(level, mutableBlockPos.set(x, y, z)), Direction.UP)) {
                return mutableBlockPos.setY(y + 1).immutable();
            }
        }
        return null;
    }
}
