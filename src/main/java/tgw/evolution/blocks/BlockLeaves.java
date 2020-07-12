package tgw.evolution.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.IShearable;
import tgw.evolution.entities.EntityFallingWeight;
import tgw.evolution.init.EvolutionBlocks;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class BlockLeaves extends Block implements IShearable, IReplaceable {

    public static final IntegerProperty DISTANCE = BlockStateProperties.DISTANCE_0_7;
    public static final BooleanProperty TREE = EvolutionBlockStateProperties.TREE;
    private static final Vec3d MOTION_MULTIPLIER = new Vec3d(0.5, 1, 0.5);

    public BlockLeaves() {
        super(Block.Properties.create(Material.LEAVES).hardnessAndResistance(0.2F, 0.2F).sound(SoundType.PLANT).doesNotBlockMovement());
        this.setDefaultState(this.stateContainer.getBaseState().with(DISTANCE, 0).with(TREE, true));
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
     * Updates this blockstate in accordance with the distance away from logs.
     */
    private static BlockState updateDistance(BlockState state, IWorld iWorld, BlockPos pos) {
        int i = 7;
        try (BlockPos.PooledMutableBlockPos mutableBlockPos = BlockPos.PooledMutableBlockPos.retain()) {
            for (Direction direction : Direction.values()) {
                mutableBlockPos.setPos(pos).move(direction);
                i = Math.min(i, getDistance(iWorld.getBlockState(mutableBlockPos)) + 1);
                if (i == 1) {
                    break;
                }
            }
        }
        return state.with(DISTANCE, i);
    }

    /**
     * Returns the distance between this block and a Log Block.
     */
    private static int getDistance(BlockState neighbor) {
        if (neighbor.getBlock() instanceof BlockLog) {
            return 0;
        }
        return neighbor.getBlock() instanceof BlockLeaves ? neighbor.get(DISTANCE) : 7;
    }

    /**
     * Called when this block should fall.
     */
    private static void fall(World worldIn, BlockPos pos) {
        EntityFallingWeight entity = new EntityFallingWeight(worldIn, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, worldIn.getBlockState(pos));
        worldIn.addEntity(entity);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!worldIn.isRemote) {
            worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn));
        }
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualFlammability(state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return ((BlockFire) EvolutionBlocks.FIRE.get()).getActualEncouragement(state);
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return false;
    }

    /**
     * Checks whether this block should fall.
     */
    private void checkFallable(World worldIn, BlockPos pos) {
        if (canFallThrough(worldIn.getBlockState(pos.down())) && pos.getY() >= 0) {
            if (worldIn.isAreaLoaded(pos.add(-32, -32, -32), pos.add(32, 32, 32))) {
                if (!worldIn.isRemote) {
                    fall(worldIn, pos);
                }
            }
            else {
                BlockState state = this.getDefaultState();
                if (worldIn.getBlockState(pos).getBlock() == this) {
                    state = worldIn.getBlockState(pos);
                    worldIn.removeBlock(pos, false);
                }
                BlockPos blockpos = pos.down();
                while (canFallThrough(worldIn.getBlockState(blockpos)) && blockpos.getY() > 0) {
                    blockpos = blockpos.down();
                }
                if (blockpos.getY() > 0) {
                    worldIn.setBlockState(blockpos.up(), state); //Forge: Fix loss of state information during world gen.
                }
            }
        }
    }

    @Override
    public boolean ticksRandomly(BlockState state) {
        return state.get(TREE) && (state.get(DISTANCE) == 0 || state.get(DISTANCE) == 7);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (state.get(TREE) && state.get(DISTANCE) == 7) {
            spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, false);
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
            entityIn.setMotionMultiplier(state, MOTION_MULTIPLIER);
        }
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IWorld world, BlockPos pos, int fortune) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
        return Collections.singletonList(new ItemStack(this));
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        worldIn.setBlockState(pos, updateDistance(state, worldIn, pos), 3);
        if (!worldIn.isRemote) {
            if (!state.get(TREE) && state.get(DISTANCE) == 7) {
                this.checkFallable(worldIn, pos);
            }
        }
    }

    @Override
    public int getOpacity(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return 1;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        int i = getDistance(facingState) + 1;
        if (i != 1 || stateIn.get(DISTANCE) != i) {
            worldIn.getPendingBlockTicks().scheduleTick(currentPos, this, 1);
        }
        return stateIn;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (worldIn.isRainingAt(pos.up()) && !worldIn.getBlockState(pos.down()).isSolid() && rand.nextInt(15) == 1) {
            double d0 = pos.getX() + rand.nextFloat();
            double d1 = pos.getY() - 0.05D;
            double d2 = pos.getZ() + rand.nextFloat();
            worldIn.addParticle(ParticleTypes.DRIPPING_WATER, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean isSolid(BlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        if (Minecraft.getInstance().gameSettings.fancyGraphics) {
            return BlockRenderLayer.CUTOUT_MIPPED;
        }
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean causesSuffocation(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return false;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE, TREE);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return updateDistance(this.getDefaultState().with(TREE, false), context.getWorld(), context.getPos());
    }

    @Nullable
    @Override
    public ItemStack getDrops(BlockState state) {
        return null;
    }
}
