package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEMolding;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.math.MathHelper;

import static tgw.evolution.init.EvolutionBStates.LAYERS_1_5;

public class BlockMolding extends BlockGeneric implements IReplaceable, EntityBlock {

    public BlockMolding() {
        super(Properties.of(Material.CLAY).dynamicShape().strength(0.0F).sound(SoundType.GRAVEL).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_5, 1));
    }

    private static void dropItemStack(Level level, int x, int y, int z, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, x + 0.5, y + 0.3, z + 0.5, stack);
        Vec3 motion = entity.getDeltaMovement();
        entity.push(-motion.x, -motion.y, -motion.z);
        level.addFreshEntity(entity);
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.isReplaceable(level.getBlockState_(x, y + 1, z)) && BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_5);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter level, int x, int y, int z, Player player) {
        return new ItemStack(EvolutionItems.CLAYBALL);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.6F;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        TEMolding tile = (TEMolding) level.getBlockEntity_(x, y, z);
        if (tile != null) {
            return tile.getHitbox(state);
        }
        return EvolutionShapes.MOLD_1;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged_(BlockState state,
                                 Level level,
                                 int x,
                                 int y,
                                 int z,
                                 Block oldBlock,
                                 int fromX,
                                 int fromY,
                                 int fromZ,
                                 boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive_(level, x, y, z)) {
                BlockPos pos = new BlockPos(x, y, z);
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEMolding(pos, state);
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hit) {
        int layers = state.getValue(LAYERS_1_5);
        if (player.getItemInHand(hand).getItem() == EvolutionItems.CLAYBALL) {
            if (layers < 5) {
                if (level.getBlockEntity_(x, y, z) instanceof TEMolding tile) {
                    level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_1_5, layers + 1));
                    tile.addLayer(layers);
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 0.8F);
                    if (!player.isCreative()) {
                        player.getItemInHand(hand).shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity_(x, y, z) instanceof TEMolding tile) {
            double hitX = (hit.getLocation().x - x) * 16;
            if (!MathHelper.rangeInclusive(hitX, 0.5, 15.5)) {
                return InteractionResult.PASS;
            }
            double hitZ = (hit.getLocation().z - z) * 16;
            if (!MathHelper.rangeInclusive(hitZ, 0.5, 15.5)) {
                return InteractionResult.PASS;
            }
            double hitY = (hit.getLocation().y - y) * 16;
            int partX = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Direction.Axis.X, hitX, hit.getDirection()));
            int partY = MathHelper.getIndex(5, 0, 15, MathHelper.hitOffset(Direction.Axis.Y, hitY, hit.getDirection()));
            int partZ = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Direction.Axis.Z, hitZ, hit.getDirection()));
//        if (!tile.matrices[partY][partX][partZ] || tile.molding.getPattern()[partY][partX][partZ]) {
//            return ActionResultType.PASS;
//        }
            level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 0.75F);
//        tile.matrices[partY][partX][partZ] = false;
            if (layers != 1) {
                int fail = tile.check();
                if (fail != -1) {
                    int count = 0;
                    for (int i = fail; i < layers; i++) {
                        count++;
                    }
                    if (layers == count) {
                        level.removeBlock(new BlockPos(x, y, z), false);
                    }
                    else {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(LAYERS_1_5, layers - count));
                    }
                    if (!level.isClientSide) {
                        dropItemStack(level, x, y, z, new ItemStack(EvolutionItems.CLAYBALL, count));
                    }
                }
            }
            tile.checkPatterns();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
