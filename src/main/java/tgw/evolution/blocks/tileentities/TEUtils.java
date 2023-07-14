package tgw.evolution.blocks.tileentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.util.constants.BlockFlags;

public final class TEUtils {

    private TEUtils() {
    }

    public static long getPosFromTag(CompoundTag tag) {
        return BlockPos.asLong(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    public static @Nullable BlockEntity loadStatic(int x, int y, int z, BlockState state, CompoundTag tag) {
        String id = tag.getString("id");
        ResourceLocation parsedId = ResourceLocation.tryParse(id);
        if (parsedId == null) {
            Evolution.error("Block entity has invalid type: {}", id);
            return null;
        }
        BlockEntityType<?> type = Registry.BLOCK_ENTITY_TYPE.get(parsedId);
        if (type == null) {
            Evolution.warn("Skipping BlockEntity with id {}", id);
            return null;
        }
        BlockEntity te = null;
        try {
            //It's fine to allocate here, since this BlockPos will be saved to the BlockEntity itself
            te = type.create(new BlockPos(x, y, z), state);
        }
        catch (Throwable t) {
            Evolution.error("Failed to create block entity {}", id, t);
            return null;
        }
        try {
            assert te != null;
            te.load(tag);
            return te;
        }
        catch (Throwable t) {
            Evolution.error("Failed to load data for block entity {}", id, t);
            return null;
        }
    }

    public static void sendRenderUpdate(BlockEntity tile) {
        tile.setChanged();
        Level level = tile.getLevel();
        BlockPos pos = tile.getBlockPos();
        assert level != null;
        BlockState state = level.getBlockState(pos);
        level.sendBlockUpdated(pos, state, state, BlockFlags.RERENDER);
    }
}
