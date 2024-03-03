package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.TEUtils;
import tgw.evolution.patches.PatchBlockEntity;
import tgw.evolution.util.math.AABBMutable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements PatchBlockEntity {

    @Unique private static final AABBMutable TE_BOX = new AABBMutable();

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static @Nullable BlockEntity loadStatic(BlockPos pos, BlockState state, CompoundTag tag) {
        Evolution.deprecatedMethod();
        return TEUtils.loadStatic(pos.getX(), pos.getY(), pos.getZ(), state, tag);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void setChanged(Level level, BlockPos pos, BlockState state) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        level.blockEntityChanged_(x, y, z);
        if (!state.isAir()) {
            level.updateNeighbourForOutputSignal_(x, y, z, state.getBlock());
        }
    }

    @Override
    public final AABBMutable _getBBForRendering() {
        return TE_BOX;
    }

    @Override
    public final BlockEntity _self() {
        return (BlockEntity) (Object) this;
    }
}
