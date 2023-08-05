package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Map;
import java.util.Random;

@Mixin(AbstractCauldronBlock.class)
public abstract class Mixin_M_AbstractCauldronBlock extends Block {

    @Shadow @Final protected static VoxelShape SHAPE;
    @Shadow @Final private static VoxelShape INSIDE;
    @Shadow @Final private Map<Item, CauldronInteraction> interactions;

    public Mixin_M_AbstractCauldronBlock(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract boolean canReceiveStalactiteDrip(Fluid fluid);

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getInteractionShape_(BlockState state, BlockGetter level, int x, int y, int z) {
        return INSIDE;
    }

    @Override
    @Overwrite
    @DeleteMethod
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        throw new AbstractMethodError();
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE;
    }

    @Shadow
    protected abstract void receiveStalactiteDrip(BlockState blockState, Level level, BlockPos blockPos, Fluid fluid);

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, Random random) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        BlockPos tipPos = PointedDripstoneBlock.findStalactiteTipAboveCauldron(level, pos);
        if (tipPos != null) {
            Fluid fluid = PointedDripstoneBlock.getCauldronFillFluidType(level, tipPos);
            if (fluid != Fluids.EMPTY && this.canReceiveStalactiteDrip(fluid)) {
                this.receiveStalactiteDrip(state, level, pos, fluid);
            }
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
    public InteractionResult use_(BlockState state,
                                  Level level,
                                  int x,
                                  int y,
                                  int z,
                                  Player player,
                                  InteractionHand hand,
                                  BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        CauldronInteraction interaction = this.interactions.get(stack.getItem());
        return interaction.interact(state, level, new BlockPos(x, y, z), player, hand, stack);
    }
}
