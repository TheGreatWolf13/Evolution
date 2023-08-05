package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.entities.misc.EntitySittable;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.items.ItemLog;
import tgw.evolution.items.ItemUtils;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;

import static tgw.evolution.init.EvolutionBStates.OCCUPIED;

public class BlockChopping extends BlockPhysics implements IReplaceable, ISittableBlock, EntityBlock, IPoppable, IWoodVariant {

    private final WoodVariant variant;

    public BlockChopping(WoodVariant variant) {
        super(Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(8.0F, 2.0F));
        this.registerDefaultState(this.defaultBlockState().setValue(OCCUPIED, false));
        this.variant = variant;
    }

    @Override
    public void attack_(BlockState state, Level level, int x, int y, int z, Direction face, double hitX, double hitY, double hitZ, Player player) {
        if (!(level.getBlockEntity_(x, y, z) instanceof TEChopping chopping)) {
            return;
        }
        if (chopping.hasLog()) {
            if (ItemUtils.isAxe(player.getMainHandItem())) {
                if (chopping.increaseBreakProgress() == 4) {
                    chopping.breakLog(player);
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.WOOD_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                else {
                    level.playSound(player, x + 0.5, y + 0.5, z + 0.5, SoundEvents.WOOD_HIT, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OCCUPIED);
    }

    @Override
    public @Range(from = 0, to = 100) int getComfort() {
        //TODO implementation
        return 0;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7F;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        if (level.getBlockEntity_(x, y, z) instanceof TEChopping te) {
            if (te.hasLog()) {
                return HarvestLevel.UNBREAKABLE;
            }
        }
        return HarvestLevel.STONE;
    }

    @Override
    public double getMass(Level level, int x, int y, int z, BlockState state) {
        return this.woodVariant().getMass() / 2.0;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return EvolutionShapes.SLAB_2_D;
    }

    @Override
    public double getYOffset() {
        return 0.3;
    }

    @Override
    public float getZOffset() {
        return -0.2f;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TEChopping(pos, state);
    }

    @Override
    public void onRemove_(BlockState state, Level level, int x, int y, int z, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            if (tile instanceof TEChopping te && te.hasLog()) {
                te.dropLog();
            }
        }
        super.onRemove_(state, level, x, y, z, newState, isMoving);
    }

    @Override
    public boolean preventsShortAttacking(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof TEChopping chopping)) {
            return super.preventsShortAttacking(level, pos, state, player);
        }
        if (chopping.hasLog() && ItemUtils.isAxe(player.getMainHandItem())) {
            return true;
        }
        return super.preventsShortAttacking(level, pos, state, player);
    }

    @Override
    public InteractionResult use_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity tile = level.getBlockEntity_(x, y, z);
        if (!(tile instanceof TEChopping chopping)) {
            return InteractionResult.PASS;
        }
        if (player.getItemInHand(hand).getItem() instanceof ItemLog && !state.getValue(OCCUPIED)) {
            if (!chopping.hasLog()) {
                chopping.setStack(player, hand);
                return InteractionResult.SUCCESS;
            }
        }
        if (!chopping.hasLog() && !state.getValue(OCCUPIED)) {
            if (!player.isCrouching() && EntitySittable.create(level, x, y, z, player)) {
                level.setBlockAndUpdate(new BlockPos(x, y, z), state.setValue(OCCUPIED, true));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (chopping.hasLog()) {
            chopping.removeStack(player);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public WoodVariant woodVariant() {
        return this.variant;
    }
}
