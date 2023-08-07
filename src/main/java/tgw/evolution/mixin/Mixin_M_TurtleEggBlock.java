package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(TurtleEggBlock.class)
public abstract class Mixin_M_TurtleEggBlock extends Block {

    @Shadow @Final public static IntegerProperty EGGS;
    @Shadow @Final public static IntegerProperty HATCH;
    @Shadow @Final private static VoxelShape MULTIPLE_EGGS_AABB;
    @Shadow @Final private static VoxelShape ONE_EGG_AABB;

    public Mixin_M_TurtleEggBlock(Properties properties) {
        super(properties);
    }

    @Contract(value = "_, _ -> _")
    @Shadow
    public static boolean onSand(BlockGetter blockGetter, BlockPos blockPos) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract void decreaseEggs(Level level, BlockPos blockPos, BlockState blockState);

    @Shadow
    protected abstract void destroyEgg(Level level, BlockState blockState, BlockPos blockPos, Entity entity, int i);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return state.getValue(EGGS) > 1 ? MULTIPLE_EGGS_AABB : ONE_EGG_AABB;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        throw new AbstractMethodError();
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        BlockPos pos = new BlockPos(x, y, z);
        if (!level.isClientSide && onSand(level, pos)) {
            level.levelEvent(LevelEvent.PARTICLES_PLANT_GROWTH, pos, 0);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void playerDestroy(Level level,
                              Player player,
                              BlockPos blockPos,
                              BlockState blockState,
                              @Nullable BlockEntity blockEntity,
                              ItemStack itemStack) {
        throw new AbstractMethodError();
    }

    @Override
    public void playerDestroy_(Level level, Player player, int x, int y, int z, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy_(level, player, x, y, z, state, te, stack);
        this.decreaseEggs(level, new BlockPos(x, y, z), state);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (this.shouldUpdateHatchLevel(level)) {
            BlockPos pos = new BlockPos(x, y, z);
            if (onSand(level, pos)) {
                int hatch = state.getValue(HATCH);
                if (hatch < 2) {
                    level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.TURTLE_EGG_CRACK, SoundSource.BLOCKS, 0.7F,
                                    0.9F + random.nextFloat() * 0.2F);
                    level.setBlock_(x, y, z, state.setValue(HATCH, hatch + 1), BlockFlags.BLOCK_UPDATE);
                }
                else {
                    level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.TURTLE_EGG_HATCH, SoundSource.BLOCKS, 0.7F,
                                    0.9F + random.nextFloat() * 0.2F);
                    level.removeBlock_(x, y, z, false);
                    for (int j = 0; j < state.getValue(EGGS); ++j) {
                        level.levelEvent_(LevelEvent.PARTICLES_DESTROY_BLOCK, x, y, z, Block.getId(state));
                        Turtle turtle = EntityType.TURTLE.create(level);
                        assert turtle != null;
                        turtle.setAge(-24_000);
                        turtle.setHomePos(pos);
                        turtle.moveTo(x + 0.3 + j * 0.2, y, z + 0.3, 0.0F, 0.0F);
                        level.addFreshEntity(turtle);
                    }
                }
            }
        }
    }

    @Shadow
    protected abstract boolean shouldUpdateHatchLevel(Level level);

    @Override
    @Overwrite
    @DeleteMethod
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        throw new AbstractMethodError();
    }

    @Override
    public void stepOn_(Level level, int x, int y, int z, BlockState state, Entity entity) {
        this.destroyEgg(level, state, new BlockPos(x, y, z), entity, 100);
        super.stepOn_(level, x, y, z, state, entity);
    }
}
