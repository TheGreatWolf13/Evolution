package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.phys.shapes.CollisionContext;
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
        super(Properties.of(Material.CLAY).strength(0.0F).sound(SoundType.GRAVEL).noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(LAYERS_1_5, 1));
    }

    private static void dropItemStack(Level level, BlockPos pos, ItemStack stack) {
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5, stack);
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
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState up = level.getBlockState(pos.above());
        return BlockUtils.isReplaceable(up) && BlockUtils.hasSolidSide(level, pos.below(), Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LAYERS_1_5);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult hitResult, BlockGetter level, BlockPos pos, Player player) {
        return new ItemStack(EvolutionItems.CLAYBALL.get());
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        TEMolding tile = (TEMolding) level.getBlockEntity(pos);
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
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            if (!state.canSurvive(level, pos)) {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEMolding(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.equals(newState)) {
            TEMolding tile = (TEMolding) level.getBlockEntity(pos);
            if (tile != null) {
                tile.sendRenderUpdate();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int layers = state.getValue(LAYERS_1_5);
        if (player.getItemInHand(hand).getItem() == EvolutionItems.CLAYBALL.get()) {
            if (layers < 5) {
                level.setBlockAndUpdate(pos, state.setValue(LAYERS_1_5, layers + 1));
                TEMolding tile = (TEMolding) level.getBlockEntity(pos);
                assert tile != null;
                tile.addLayer(layers);
                level.playSound(player, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5F, 0.8F);
                if (!player.isCreative()) {
                    player.getItemInHand(hand).shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (!player.getItemInHand(hand).isEmpty()) {
            return InteractionResult.PASS;
        }
        TEMolding tile = (TEMolding) level.getBlockEntity(pos);
        if (tile == null) {
            return InteractionResult.PASS;
        }
        double hitX = (hit.getLocation().x - pos.getX()) * 16;
        if (!MathHelper.rangeInclusive(hitX, 0.5, 15.5)) {
            return InteractionResult.PASS;
        }
        double hitZ = (hit.getLocation().z - pos.getZ()) * 16;
        if (!MathHelper.rangeInclusive(hitZ, 0.5, 15.5)) {
            return InteractionResult.PASS;
        }
        double hitY = (hit.getLocation().y - pos.getY()) * 16;
        int x = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Direction.Axis.X, hitX, hit.getDirection()));
        int y = MathHelper.getIndex(5, 0, 15, MathHelper.hitOffset(Direction.Axis.Y, hitY, hit.getDirection()));
        int z = MathHelper.getIndex(5, 0.5, 15.5, MathHelper.hitOffset(Direction.Axis.Z, hitZ, hit.getDirection()));
//        if (!tile.matrices[y][x][z] || tile.molding.getPattern()[y][x][z]) {
//            return ActionResultType.PASS;
//        }
        level.playSound(player, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 0.75F);
//        tile.matrices[y][x][z] = false;
        if (layers != 1) {
            int fail = tile.check();
            if (fail != -1) {
                int count = 0;
                for (int i = fail; i < layers; i++) {
                    count++;
                }
                if (layers == count) {
                    level.removeBlock(pos, false);
                }
                else {
                    level.setBlockAndUpdate(pos, state.setValue(LAYERS_1_5, layers - count));
                }
                if (!level.isClientSide) {
                    dropItemStack(level, pos, new ItemStack(EvolutionItems.CLAYBALL.get(), count));
                }
            }
        }
        tile.sendRenderUpdate();
        tile.checkPatterns();
        return InteractionResult.SUCCESS;
    }
}
