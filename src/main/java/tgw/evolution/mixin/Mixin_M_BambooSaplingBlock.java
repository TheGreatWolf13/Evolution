package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(BambooSaplingBlock.class)
public abstract class Mixin_M_BambooSaplingBlock extends Block implements BonemealableBlock {

    @Shadow @Final protected static VoxelShape SAPLING_SHAPE;

    public Mixin_M_BambooSaplingBlock(Properties properties) {
        super(properties);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return level.getBlockState_(x, y - 1, z).is(BlockTags.BAMBOO_PLANTABLE_ON);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public float getDestroyProgress(BlockState blockState, Player player, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public float getDestroyProgress_(BlockState state, Player player, BlockGetter level, int x, int y, int z) {
        return player.getMainHandItem().getItem() instanceof SwordItem ? 1.0F : super.getDestroyProgress_(state, player, level, x, y, z);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return this.moveShapeByOffset(SAPLING_SHAPE, x, z);
    }

    @Shadow
    protected abstract void growBamboo(Level level, BlockPos blockPos);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (random.nextInt(3) == 0 && level.isEmptyBlock_(x, y + 1, z) && level.getRawBrightness_(BlockPos.asLong(x, y + 1, z), 0) >= 9) {
            this.growBamboo(level, new BlockPos(x, y, z));
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    @DeleteMethod
    public BlockState updateShape(BlockState blockState,
                                  Direction direction,
                                  BlockState blockState2,
                                  LevelAccessor levelAccessor,
                                  BlockPos blockPos,
                                  BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        if (!state.canSurvive_(level, x, y, z)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (from == Direction.UP && fromState.is(Blocks.BAMBOO)) {
            level.setBlock(new BlockPos(x, y, z), Blocks.BAMBOO.defaultBlockState(), 2);
        }
        return super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
    }
}
