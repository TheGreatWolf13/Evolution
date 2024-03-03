package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tgw.evolution.Evolution;

@Mixin(ClipContext.Block.class)
public abstract class MixinClipContext_Block implements ClipContext.ShapeGetter {

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public VoxelShape get(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Evolution.deprecatedMethod();
        return this.get_(state, level, pos.getX(), pos.getY(), pos.getZ(), context instanceof EntityCollisionContext c ? c.getEntity() : null);
    }

    @Override
    public VoxelShape get_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return switch (((Enum<ClipContext.Block>) (Object) this).ordinal()) {
            case 0 -> state.getCollisionShape_(level, x, y, z, entity);
            case 1 -> state.getShape_(level, x, y, z, entity);
            case 2 -> state.getVisualShape_(level, x, y, z, entity);
            default -> state.is(BlockTags.FALL_DAMAGE_RESETTING) ? Shapes.block() : Shapes.empty();
        };
    }
}
