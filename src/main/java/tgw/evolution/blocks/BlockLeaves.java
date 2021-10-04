package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IForgeShearable;
import tgw.evolution.entities.misc.EntityFallingWeight;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.DirectionUtil;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.DISTANCE_0_7;
import static tgw.evolution.init.EvolutionBStates.TREE;

public class BlockLeaves extends BlockGeneric implements IReplaceable, IForgeShearable {

    private static final Vector3d MOTION_MULTIPLIER = new Vector3d(0.5, 1, 0.5);

    public BlockLeaves() {
        super(Properties.of(Material.LEAVES).strength(0.2F, 0.2F).sound(SoundType.GRASS).noCollission());
        this.registerDefaultState(this.defaultBlockState().setValue(DISTANCE_0_7, 0).setValue(TREE, true));
    }

    /**
     * Returns whether this block can fall through.
     */
    public static boolean canFallThrough(BlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return state.isAir() || block == EvolutionBlocks.FIRE.get() || material.isLiquid() || material.isReplaceable();
    }

    /**
     * Called when this block should fall.
     */
    private static void fall(World world, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.getBlockState(pos));
        world.addFreshEntity(entity);
    }

    /**
     * Returns the distance between this block and a Log Block.
     */
    private static int getDistance(BlockState neighbor) {
        if (neighbor.getBlock() instanceof BlockLog) {
            return 0;
        }
        return neighbor.getBlock() instanceof BlockLeaves ? neighbor.getValue(DISTANCE_0_7) : 7;
    }

    /**
     * Updates this blockstate in accordance with the distance away from logs.
     */
    private static BlockState updateDistance(BlockState state, IWorld world, BlockPos pos) {
        int i = 7;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : DirectionUtil.ALL) {
            mutable.setWithOffset(pos, direction);
            i = Math.min(i, getDistance(world.getBlockState(mutable)) + 1);
            if (i == 1) {
                break;
            }
        }
        return state.setValue(DISTANCE_0_7, i);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (world.isRainingAt(pos.above()) && !world.getBlockState(pos.below()).canOcclude() && rand.nextInt(15) == 1) {
            double x = pos.getX() + rand.nextFloat();
            double y = pos.getY() - 0.05;
            double z = pos.getZ() + rand.nextFloat();
            world.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return false;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    /**
     * Checks whether this block should fall.
     */
    private void checkFallable(World world, BlockPos pos) {
        if (canFallThrough(world.getBlockState(pos.below())) && pos.getY() >= 0) {
            if (world.isAreaLoaded(pos, 32)) {
                if (!world.isClientSide) {
                    fall(world, pos);
                }
            }
            else {
                BlockState state = this.defaultBlockState();
                if (world.getBlockState(pos).getBlock() == this) {
                    state = world.getBlockState(pos);
                    world.removeBlock(pos, false);
                }
                BlockPos.Mutable posDown = new BlockPos.Mutable();
                posDown.setWithOffset(pos, Direction.DOWN);
                while (canFallThrough(world.getBlockState(posDown)) && posDown.getY() > 0) {
                    posDown.move(Direction.DOWN);
                }
                if (posDown.getY() > 0) {
                    world.setBlockAndUpdate(posDown.move(Direction.UP), state);
                }
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE_0_7, TREE);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (entity instanceof LivingEntity) {
            entity.makeStuckInBlock(state, MOTION_MULTIPLIER);
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualEncouragement(state);
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return EvolutionBlocks.FIRE.get().getActualFlammability(state);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.2f;
    }

    @Override
    public int getLightBlock(BlockState state, IBlockReader world, BlockPos pos) {
        return 1;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return updateDistance(this.defaultBlockState().setValue(TREE, false), context.getLevel(), context.getClickedPos());
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(TREE) && (state.getValue(DISTANCE_0_7) == 0 || state.getValue(DISTANCE_0_7) == 7);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!world.isClientSide) {
            world.getBlockTicks().scheduleTick(pos, this, 2);
        }
    }

    @Override
    public List<ItemStack> onSheared(PlayerEntity player, ItemStack item, World world, BlockPos pos, int fortune) {
        world.setBlock(pos, Blocks.AIR.defaultBlockState(), BlockFlags.NOTIFY_UPDATE_AND_RERENDER);
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            dropResources(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockAndUpdate(pos, updateDistance(state, world, pos));
        if (!state.getValue(TREE) && state.getValue(DISTANCE_0_7) == 7) {
            this.checkFallable(world, pos);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || state.getValue(DISTANCE_0_7) != i) {
            world.getBlockTicks().scheduleTick(currentPos, this, 1);
        }
        return state;
    }
}
