//package tgw.evolution.blocks;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.sounds.SoundEvent;
//import net.minecraft.world.InteractionHand;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.BlockGetter;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.SoundType;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.block.state.StateDefinition;
//import net.minecraft.world.level.material.Material;
//import net.minecraft.world.phys.BlockHitResult;
//import net.minecraft.world.phys.shapes.VoxelShape;
//import org.jetbrains.annotations.Nullable;
//import org.jetbrains.annotations.Range;
//import tgw.evolution.blocks.util.BlockUtils;
//import tgw.evolution.entities.misc.EntityFallingPeat;
//import tgw.evolution.init.EvolutionBlocks;
//import tgw.evolution.init.EvolutionShapes;
//import tgw.evolution.init.EvolutionSounds;
//import tgw.evolution.util.constants.BlockFlags;
//import tgw.evolution.util.math.DirectionUtil;
//
//import java.util.Random;
//
//import static tgw.evolution.init.EvolutionBStates.LAYERS_1_4;
//
//public class BlockPeat extends BlockPhysics implements IReplaceable, IAir {
//    public BlockPeat() {
//        super(Properties.of(Material.DIRT).strength(2.0f, 0.5f).sound(SoundType.GRAVEL));
//        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_4, 1));
//    }
//
//    private static boolean canFallThrough(BlockState state, BlockGetter level, int x, int y, int z) {
//        if (!BlockGravity.canFallThrough(state)) {
//            return false;
//        }
//        if (state.getBlock() instanceof BlockPeat) {
//            return state.getValue(LAYERS_1_4) != 4;
//        }
//        return state.getCollisionShape_(level, x, y, z).isEmpty();
//    }
//
//    public static void checkFallable(Level level, int x, int y, int z, BlockState state) {
//        BlockState stateDown = level.getBlockState_(x, y - 1, z);
//        int layers = 0;
//        if (stateDown.getBlock() == EvolutionBlocks.PEAT) {
//            layers = stateDown.getValue(LAYERS_1_4);
//        }
//        if (layers == 4) {
//            return;
//        }
//        if (!level.isEmptyBlock_(x, y - 1, z)) {
//            if (!canFallThrough(stateDown, level, x, y - 1, z)) {
//                return;
//            }
//        }
//        if (y < level.getMinBuildHeight()) {
//            return;
//        }
//        level.removeBlock_(x, y, z, true);
//        EntityFallingPeat entity = new EntityFallingPeat(level, x + 0.5, y, z + 0.5, state.getValue(LAYERS_1_4));
//        level.addFreshEntity(entity);
//        entity.playSound(EvolutionSounds.SOIL_COLLAPSE, 0.25F, 1.0F);
//        for (Direction dir : DirectionUtil.ALL_EXCEPT_DOWN) {
//            BlockUtils.scheduleBlockTick(level, x + dir.getStepX(), y + dir.getStepY(), z + dir.getStepZ());
//        }
//    }
//
//    public static void placeLayersOn(Level level, BlockPos pos, int layers) {
//        BlockState state = level.getBlockState(pos);
//        if (state.getBlock() == EvolutionBlocks.PEAT) {
//            for (int i = 1; i <= 4; i++) {
//                if (state.getValue(LAYERS_1_4) == i) {
//                    if (i + layers > 4) {
//                        int remain = i + layers - 4;
//                        level.setBlock(pos, state.setValue(LAYERS_1_4, 4), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//                        level.setBlock(pos.above(), EvolutionBlocks.PEAT.defaultBlockState().setValue(LAYERS_1_4, remain),
//                                       BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//                        return;
//                    }
//                    level.setBlock(pos, state.setValue(LAYERS_1_4, i + layers), BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//                    return;
//                }
//            }
//        }
//        if (state.getBlock() instanceof IReplaceable) {
//            level.setBlock(pos, EvolutionBlocks.PEAT.defaultBlockState().setValue(LAYERS_1_4, layers),
//                           BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//            return;
//        }
//        if (state.getMaterial().isReplaceable()) {
//            level.setBlock(pos, EvolutionBlocks.PEAT.defaultBlockState().setValue(LAYERS_1_4, layers),
//                           BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//        }
//    }
//
//    @Override
//    public boolean allowsFrom(BlockState state, Direction from) {
//        if (from == Direction.DOWN) {
//            return false;
//        }
//        return state.getValue(LAYERS_1_4) != 4;
//    }
//
//    @Override
//    public boolean canBeReplacedByFluid(BlockState state) {
//        return state.getValue(LAYERS_1_4) < 4;
//    }
//
//    @Override
//    public boolean canBeReplacedByRope(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean canBeReplaced_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult
//    hitResult) {
//        if (player.getItemInHand(hand).getItem() == this.asItem() && state.getValue(LAYERS_1_4) < 4) {
//            return hitResult.getDirection() == Direction.UP;
//        }
//        return false;
//    }
//
//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
//        builder.add(LAYERS_1_4);
//    }
//
//    @Override
//    public @Nullable SoundEvent fallingSound() {
//        return EvolutionSounds.SOIL_COLLAPSE;
//    }
//
//    @Override
//    public float getFrictionCoefficient(BlockState state) {
//        return 0.63f;
//    }
//
//    @Override
//    public double getMass(Level level, int x, int y, int z, BlockState state) {
//        return state.getValue(LAYERS_1_4) * (1_156 / 4.0);
//    }
//
//    @Override
//    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
//        return EvolutionShapes.SLAB_4_D[state.getValue(LAYERS_1_4) - 1];
//    }
//
//    @Override
//    public @Nullable BlockState getStateForPlacement_(Level level,
//                                                      int x,
//                                                      int y,
//                                                      int z,
//                                                      Player player,
//                                                      InteractionHand hand,
//                                                      BlockHitResult hitResult) {
//        BlockState state = level.getBlockState_(x, y, z);
//        if (state.getBlock() == this) {
//            int layers = state.getValue(LAYERS_1_4);
//            return state.setValue(LAYERS_1_4, Math.min(layers + 1, 4));
//        }
//        return super.getStateForPlacement_(level, x, y, z, player, hand, hitResult);
//    }
//
//    @Override
//    public @Range(from = 1, to = 31) int increment(BlockState state, Direction from) {
//        return 1;
//    }
//
//    @Override
//    public boolean isReplaceable(BlockState state) {
//        return state.getValue(LAYERS_1_4) != 4;
//    }
//
//    @Override
//    public void playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player) {
//        BlockUtils.scheduleBlockTick(level, x, y + 1, z);
//        if (player.isCreative() || state.getValue(LAYERS_1_4) == 1) {
//            super.playerWillDestroy_(level, x, y, z, state, player);
//        }
//        else {
//            level.setBlock(new BlockPos(x, y, z), state.setValue(LAYERS_1_4, state.getValue(LAYERS_1_4) - 1),
//                           level.isClientSide ?
//                           BlockFlags.RENDER_MAINTHREAD | BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE :
//                           BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
//        }
//    }
//
//    @Override
//    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
//        checkFallable(level, x, y, z, state);
//    }
//
//    @Override
//    public BlockState updateShape_(BlockState state,
//                                   Direction from,
//                                   BlockState fromState,
//                                   LevelAccessor level,
//                                   int x,
//                                   int y,
//                                   int z,
//                                   int fromX,
//                                   int fromY,
//                                   int fromZ) {
//        return !state.canSurvive_(level, x, y, z) ?
//               Blocks.AIR.defaultBlockState() :
//               super.updateShape_(state, from, fromState, level, x, y, z, fromX, fromY, fromZ);
//    }
//}
