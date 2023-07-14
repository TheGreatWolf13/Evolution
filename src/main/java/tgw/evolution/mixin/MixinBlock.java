package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchBlock;
import tgw.evolution.util.constants.HarvestLevel;

import java.util.List;

@Mixin(Block.class)
public abstract class MixinBlock extends BlockBehaviour implements PatchBlock {

    @Shadow @Final private static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE;

    public MixinBlock(Properties properties) {
        super(properties);
    }

    /**
     * @author TheGreatWolf
     * @reason Remove lambda allocation
     */
    @Overwrite
    public static void dropResources(BlockState state, LootContext.Builder builder) {
        ServerLevel level = builder.getLevel();
        BlockPos pos = new BlockPos(builder.getParameter(LootContextParams.ORIGIN));
        List<ItemStack> drops = state.getDrops(builder);
        for (int i = 0, l = drops.size(); i < l; i++) {
            popResource(level, pos, drops.get(i));
        }
        state.spawnAfterBreak(level, pos, ItemStack.EMPTY);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid lambda allocation
     */
    @Overwrite
    public static void dropResources(BlockState state, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, null);
            for (int i = 0, l = drops.size(); i < l; i++) {
                popResource(level, pos, drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid lambda allocation
     */
    @Overwrite
    public static void dropResources(BlockState state, LevelAccessor level, BlockPos pos, @javax.annotation.Nullable BlockEntity te) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, te);
            for (int i = 0, l = drops.size(); i < l; i++) {
                popResource(serverLevel, pos, drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid lambda allocation
     */
    @Overwrite
    public static void dropResources(BlockState state,
                                     Level level,
                                     BlockPos pos,
                                     @javax.annotation.Nullable BlockEntity te,
                                     Entity entity,
                                     ItemStack tool) {
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> drops = getDrops(state, serverLevel, pos, te, entity, tool);
            for (int i = 0, len = drops.size(); i < len; i++) {
                popResource(level, pos, drops.get(i));
            }
            state.spawnAfterBreak(serverLevel, pos, tool);
        }

    }

    @Shadow
    public static List<ItemStack> getDrops(BlockState pState,
                                           ServerLevel pLevel,
                                           BlockPos pPos,
                                           @Nullable BlockEntity pBlockEntity) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static List<ItemStack> getDrops(BlockState pState,
                                           ServerLevel pLevel,
                                           BlockPos pPos,
                                           @Nullable BlockEntity pBlockEntity,
                                           @Nullable Entity pEntity,
                                           ItemStack pTool) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void popResource(Level pLevel, BlockPos pPos, ItemStack pStack) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason Improve culling.
     */
    @Overwrite
    public static boolean shouldRenderFace(BlockState state, BlockGetter level, BlockPos offset, Direction face, BlockPos pos) {
        BlockState adjacentState = level.getBlockState(pos);
        if (state.getBlock().shouldCull(level, state, offset, adjacentState, pos, face)) {
            return false;
        }
        if (adjacentState.canOcclude()) {
            Block.BlockStatePairKey pairKey = new Block.BlockStatePairKey(state, adjacentState, face);
            Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey> map = OCCLUSION_CACHE.get();
            byte b = map.getAndMoveToFirst(pairKey);
            if (b != 127) {
                return b != 0;
            }
            VoxelShape shape = state.getFaceOcclusionShape(level, offset, face);
            if (shape.isEmpty()) {
                return true;
            }
            VoxelShape voxelshape1 = adjacentState.getFaceOcclusionShape(level, pos, face.getOpposite());
            boolean flag = Shapes.joinIsNotEmpty(shape, voxelshape1, BooleanOp.ONLY_FIRST);
            if (map.size() == 2_048) {
                map.removeLastByte();
            }
            map.putAndMoveToFirst(pairKey, (byte) (flag ? 1 : 0));
            return flag;
        }
        return true;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        return this.getCloneItemStack(level, pos, state);
    }

    @Shadow
    public abstract ItemStack getCloneItemStack(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState);

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.8f;
    }

    @Override
    public int getHarvestLevel(BlockState state, @Nullable Level level, @Nullable BlockPos pos) {
        return HarvestLevel.HAND;
    }
}
