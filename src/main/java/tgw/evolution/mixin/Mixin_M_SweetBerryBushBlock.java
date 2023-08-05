package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.util.constants.BlockFlags;

import java.util.Random;

@Mixin(SweetBerryBushBlock.class)
public abstract class Mixin_M_SweetBerryBushBlock extends BushBlock implements BonemealableBlock {

    @Shadow @Final public static IntegerProperty AGE;
    @Shadow @Final private static VoxelShape SAPLING_SHAPE;
    @Shadow @Final private static VoxelShape MID_GROWTH_SHAPE;

    public Mixin_M_SweetBerryBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        if (state.getValue(AGE) == 0) {
            return SAPLING_SHAPE;
        }
        return state.getValue(AGE) < 3 ? MID_GROWTH_SHAPE : super.getShape_(state, level, x, y, z, entity);
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void randomTick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        int age = state.getValue(AGE);
        if (age < 3 && random.nextInt(5) == 0 && level.getRawBrightness_(BlockPos.asLong(x, y + 1, z), 0) >= 9) {
            level.setBlock_(x, y, z, state.setValue(AGE, age + 1), BlockFlags.BLOCK_UPDATE);
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult use(BlockState blockState,
                                 Level level,
                                 BlockPos blockPos,
                                 Player player,
                                 InteractionHand interactionHand,
                                 BlockHitResult blockHitResult) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        boolean isFullyGrown = age == 3;
        if (!isFullyGrown && player.getItemInHand(hand).is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        if (age > 1) {
            int j = 1 + level.random.nextInt(2);
            BlockPos pos = new BlockPos(x, y, z);
            popResource(level, pos, new ItemStack(Items.SWEET_BERRIES, j + (isFullyGrown ? 1 : 0)));
            level.playSound(null, x + 0.5, y + 0.5, z + 0.5, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F,
                            0.8F + level.random.nextFloat() * 0.4F);
            level.setBlock(pos, state.setValue(AGE, 1), 2);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use_(state, level, x, y, z, player, hand, hitResult);
    }
}
