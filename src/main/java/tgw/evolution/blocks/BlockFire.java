package tgw.evolution.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockFire extends BlockEvolution implements IReplaceable, IFireSource {

    public static final SoundType FIRE = new SoundType(0.5f,
                                                       2.6f,
                                                       SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                                       SoundEvents.BLOCK_WOOL_STEP,
                                                       SoundEvents.BLOCK_WOOL_PLACE,
                                                       SoundEvents.BLOCK_WOOL_HIT,
                                                       SoundEvents.BLOCK_WOOL_FALL);
    private final Object2IntMap<Block> encouragements = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Block> flammabilities = new Object2IntOpenHashMap<>();

    public BlockFire() {
        super(Properties.create(Material.FIRE).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0).lightValue(15).sound(FIRE).noDrops());
        this.setDefaultState(this.getDefaultState()
                                 .with(AGE_0_15, 0)
                                 .with(NORTH, false)
                                 .with(EAST, false)
                                 .with(SOUTH, false)
                                 .with(WEST, false)
                                 .with(UP, false));
    }

    private static int getNeighborEncouragement(IWorldReader worldIn, BlockPos pos) {
        if (!worldIn.isAirBlock(pos)) {
            return 0;
        }
        int i = 0;
        for (Direction direction : Direction.values()) {
            BlockState blockstate = worldIn.getBlockState(pos.offset(direction));
            i = Math.max(blockstate.getFlammability(worldIn, pos.offset(direction), direction.getOpposite()), i);
        }
        return i;
    }

    public static void init() {
        BlockFire fireblock = EvolutionBlocks.FIRE.get();
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_ACACIA, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_ASPEN, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_BIRCH, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_CEDAR, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_EBONY, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_ELM, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_EUCALYPTUS, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_FIR, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_KAPOK, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_MANGROVE, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_MAPLE, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_OAK, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_OLD_OAK, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_PALM, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_PINE, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_REDWOOD, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_SPRUCE, 5, 20);
        fireblock.setFireInfo(EvolutionBlocks.PLANKS_WILLOW, 5, 20);

        //        fireblock.setFireInfo(Blocks.OAK_SLAB, 5, 20);

        //        fireblock.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);

        //        fireblock.setFireInfo(Blocks.OAK_FENCE, 5, 20);

        //        fireblock.setFireInfo(Blocks.OAK_STAIRS, 5, 20);

        fireblock.setFireInfo(EvolutionBlocks.LOG_ACACIA, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_ASPEN, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_BIRCH, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_CEDAR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_EBONY, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_ELM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_EUCALYPTUS, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_FIR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_KAPOK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_MANGROVE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_MAPLE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_OLD_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PALM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PINE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_REDWOOD, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_SPRUCE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_WILLOW, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_ACACIA, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_ASPEN, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_BIRCH, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_CEDAR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_EBONY, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_ELM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_EUCALYPTUS, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_FIR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_KAPOK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_MANGROVE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_MAPLE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_OLD_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_PALM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_PINE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_REDWOOD, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_SPRUCE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.CHOPPING_BLOCK_WILLOW, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_ACACIA, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_ASPEN, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_BIRCH, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_CEDAR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_EBONY, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_ELM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_EUCALYPTUS, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_FIR, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_KAPOK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_MANGROVE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_MAPLE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_OLD_OAK, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_PALM, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_PINE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_REDWOOD, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_SPRUCE, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LOG_PILE_WILLOW, 5, 5);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_ACACIA, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_ASPEN, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_BIRCH, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_CEDAR, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_EBONY, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_ELM, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_EUCALYPTUS, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_FIR, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_KAPOK, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_MANGROVE, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_MAPLE, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_OAK, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_OLD_OAK, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_PALM, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_PINE, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_REDWOOD, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_SPRUCE, 30, 60);
        fireblock.setFireInfo(EvolutionBlocks.LEAVES_WILLOW, 30, 60);

        //        fireblock.setFireInfo(Blocks.BOOKSHELF, 30, 20);
        //        fireblock.setFireInfo(Blocks.TNT, 15, 100);

        fireblock.setFireInfo(EvolutionBlocks.GRASS, 60, 100);
        fireblock.setFireInfo(EvolutionBlocks.TALLGRASS, 60, 100);
        //        fireblock.setFireInfo(Blocks.WHITE_WOOL, 30, 60);

        //        fireblock.setFireInfo(Blocks.VINE, 15, 100);
        //        fireblock.setFireInfo(Blocks.COAL_BLOCK, 5, 5);
        //        fireblock.setFireInfo(Blocks.HAY_BLOCK, 60, 20);
        //        fireblock.setFireInfo(Blocks.WHITE_CARPET, 60, 20);

        //        fireblock.setFireInfo(Blocks.DRIED_KELP_BLOCK, 30, 60);
        //        fireblock.setFireInfo(Blocks.BAMBOO, 60, 60);
        //        fireblock.setFireInfo(Blocks.SCAFFOLDING, 60, 60);
        //        fireblock.setFireInfo(Blocks.LECTERN, 30, 20);
        //        fireblock.setFireInfo(Blocks.COMPOSTER, 5, 20);
        //        fireblock.setFireInfo(Blocks.SWEET_BERRY_BUSH, 60, 100);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
        if (rand.nextInt(8) == 0) {
            world.playSound(pos.getX() + 0.5,
                            pos.getY() + 0.5,
                            pos.getZ() + 0.5,
                            SoundEvents.BLOCK_FIRE_AMBIENT,
                            SoundCategory.BLOCKS,
                            1.0F + rand.nextFloat(),
                            rand.nextFloat() * 0.7F + 0.3F,
                            false);
        }
        BlockPos posDown = pos.down();
        BlockState stateDown = world.getBlockState(posDown);
        if (!this.canCatchFire(world, posDown, Direction.UP) && !Block.hasSolidSide(stateDown, world, posDown, Direction.UP)) {
            if (this.canCatchFire(world, posDown.west(), Direction.EAST)) {
                for (int j = 0; j < 2; ++j) {
                    double x = pos.getX() + rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(world, pos.east(), Direction.WEST)) {
                for (int k = 0; k < 2; ++k) {
                    double x = (pos.getX() + 1) - rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(world, pos.north(), Direction.SOUTH)) {
                for (int l = 0; l < 2; ++l) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble() * 0.1;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(world, pos.south(), Direction.NORTH)) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = (pos.getZ() + 1) - rand.nextDouble() * 0.1;
                    world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(world, pos.up(), Direction.DOWN)) {
                for (int j1 = 0; j1 < 2; ++j1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = (pos.getY() + 1) - rand.nextDouble() * 0.1;
                    double z = pos.getZ() + rand.nextDouble();
                    world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
        }
        else {
            for (int i = 0; i < 3; ++i) {
                double x = pos.getX() + rand.nextDouble();
                double y = pos.getY() + rand.nextDouble() * 0.5 + 0.5;
                double z = pos.getZ() + rand.nextDouble();
                world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            }
        }
    }

    private boolean areNeighborsFlammable(IBlockReader world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(world, pos.offset(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    @Deprecated //Forge: Use canCatchFire with more context
    public boolean canBurn(BlockState state) {
        return this.getActualEncouragement(state) > 0;
    }

    /**
     * Side sensitive version that calls the block function.
     *
     * @param world The current world
     * @param pos   Block position
     * @param face  The side the fire is coming from
     * @return True if the face can catch fire.
     */
    public boolean canCatchFire(IBlockReader world, BlockPos pos, Direction face) {
        return world.getBlockState(pos).isFlammable(world, pos, face);
    }

    protected boolean canDie(World world, BlockPos pos) {
        return world.isRainingAt(pos) ||
               world.isRainingAt(pos.west()) ||
               world.isRainingAt(pos.east()) ||
               world.isRainingAt(pos.north()) ||
               world.isRainingAt(pos.south());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE_0_15, NORTH, EAST, SOUTH, WEST, UP);
    }

    public int getActualEncouragement(BlockState state) {
        return state.has(FLUIDLOGGED) && state.get(FLUIDLOGGED) ? 0 : this.encouragements.getInt(state.getBlock());
    }

    public int getActualFlammability(BlockState state) {
        return state.has(FLUIDLOGGED) && state.get(FLUIDLOGGED) ? 0 : this.flammabilities.getInt(state.getBlock());
    }

    @Override
    public ItemStack getDrops(World world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = VoxelShapes.empty();
        if (state.get(NORTH)) {
            shape = EvolutionHitBoxes.SIXTEENTH_SLAB_NORTH_1;
        }
        if (state.get(SOUTH)) {
            shape = VoxelShapes.combine(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_SOUTH_1, IBooleanFunction.OR);
        }
        if (state.get(EAST)) {
            shape = VoxelShapes.combine(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_EAST_1, IBooleanFunction.OR);
        }
        if (state.get(WEST)) {
            shape = VoxelShapes.combine(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_WEST_1, IBooleanFunction.OR);
        }
        if (state.get(UP)) {
            shape = VoxelShapes.combine(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_UPPER_1, IBooleanFunction.OR);
        }
        if (shape == VoxelShapes.empty()) {
            return EvolutionHitBoxes.SIXTEENTH_SLAB_LOWER_1;
        }
        return shape;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getStateForPlacement(context.getWorld(), context.getPos());
    }

    public BlockState getStateForPlacement(IBlockReader worldIn, BlockPos pos) {
        BlockPos posDown = pos.down();
        BlockState stateDown = worldIn.getBlockState(posDown);
        if (!this.canCatchFire(worldIn, pos, Direction.UP) && !Block.hasSolidSide(stateDown, worldIn, posDown, Direction.UP)) {
            BlockState state = this.getDefaultState();
            for (Direction direction : MathHelper.DIRECTIONS_EXCEPT_DOWN) {
                BooleanProperty booleanProperty = directionToProperty(direction);
                if (booleanProperty != null) {
                    state = state.with(booleanProperty, this.canCatchFire(worldIn, pos.offset(direction), direction.getOpposite()));
                }
            }
            return state;
        }
        return this.getDefaultState();
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return true;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        return worldIn.getBlockState(blockpos).func_224755_d(worldIn, blockpos, Direction.UP) || this.areNeighborsFlammable(worldIn, pos);
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (!state.isValidPosition(worldIn, pos)) {
                worldIn.removeBlock(pos, false);
            }
            else {
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn) + worldIn.rand.nextInt(10));
                BlockState stateDown = worldIn.getBlockState(pos.down());
                if (stateDown.getBlock() == EvolutionBlocks.PIT_KILN.get() && stateDown.get(LAYERS_0_16) == 16) {
                    if (BlockPitKiln.canBurn(worldIn.getWorld(), pos.down())) {
                        TEPitKiln tile = (TEPitKiln) worldIn.getTileEntity(pos.down());
                        tile.start();
                    }
                }
            }
        }
    }

    public void setFireInfo(RegistryObject<? extends Block> block, int encouragement, int flammability) {
        if (block.get() == Blocks.AIR) {
            throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        }
        this.encouragements.put(block.get(), encouragement);
        this.flammabilities.put(block.get(), flammability);
    }

    @Override
    public void tick(BlockState state, World world, BlockPos pos, Random random) {
        if (world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            if (!world.isAreaLoaded(pos, 2)) {
                return;
            }
            if (!state.isValidPosition(world, pos)) {
                world.removeBlock(pos, false);
            }
            BlockState stateDown = world.getBlockState(pos.down());
            boolean isDownFireSource = stateDown.isFireSource(world, pos.down(), Direction.UP);
            int age = state.get(AGE_0_15);
            if (world.isRaining() && this.canDie(world, pos) && random.nextFloat() < 0.2F + age * 0.03F) {
                world.removeBlock(pos, false);
            }
            else {
                int j = Math.min(15, age + random.nextInt(3) / 2);
                if (age != j) {
                    state = state.with(AGE_0_15, j);
                    world.setBlockState(pos, state, 4);
                }
                if (!isDownFireSource) {
                    world.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(world) + random.nextInt(10));
                    if (!this.areNeighborsFlammable(world, pos)) {
                        BlockPos blockpos = pos.down();
                        if (!world.getBlockState(blockpos).func_224755_d(world, blockpos, Direction.UP) || age > 3) {
                            world.removeBlock(pos, false);
                        }
                        return;
                    }
                    if (age == 15 && random.nextInt(4) == 0 && !this.canCatchFire(world, pos.down(), Direction.UP)) {
                        world.removeBlock(pos, false);
                        return;
                    }
                }
                boolean isHighHumidity = world.isBlockinHighHumidity(pos);
                int humidyModifier = isHighHumidity ? -50 : 0;
                this.tryCatchFire(world, pos.east(), 300 + humidyModifier, random, age, Direction.WEST);
                this.tryCatchFire(world, pos.west(), 300 + humidyModifier, random, age, Direction.EAST);
                this.tryCatchFire(world, pos.down(), 250 + humidyModifier, random, age, Direction.UP);
                this.tryCatchFire(world, pos.up(), 250 + humidyModifier, random, age, Direction.DOWN);
                this.tryCatchFire(world, pos.north(), 300 + humidyModifier, random, age, Direction.SOUTH);
                this.tryCatchFire(world, pos.south(), 300 + humidyModifier, random, age, Direction.NORTH);
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = -1; y <= 4; ++y) {
                            if (x != 0 || y != 0 || z != 0) {
                                int k1 = 100;
                                if (y > 1) {
                                    k1 += (y - 1) * 100;
                                }
                                mutableBlockPos.setPos(pos).move(x, y, z);
                                int l1 = getNeighborEncouragement(world, mutableBlockPos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                                    if (isHighHumidity) {
                                        i2 /= 2;
                                    }
                                    if (i2 > 0 && random.nextInt(k1) <= i2 && (!world.isRaining() || !this.canDie(world, mutableBlockPos))) {
                                        int j2 = Math.min(15, age + random.nextInt(5) / 4);
                                        world.setBlockState(mutableBlockPos,
                                                            this.getStateForPlacement(world, mutableBlockPos).with(AGE_0_15, j2),
                                                            BlockFlags.NOTIFY_AND_UPDATE);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public int tickRate(IWorldReader world) {
        return 30;
    }

    private void tryCatchFire(World world, BlockPos pos, int chance, Random random, int age, Direction face) {
        int i = world.getBlockState(pos).getFlammability(world, pos, face);
        if (random.nextInt(chance) < i) {
            BlockState state = world.getBlockState(pos);
            if (random.nextInt(age + 10) < 5 && !world.isRainingAt(pos)) {
                int j = Math.min(age + random.nextInt(5) / 4, 15);
                world.setBlockState(pos, this.getStateForPlacement(world, pos).with(AGE_0_15, j), 3);
            }
            else {
                world.removeBlock(pos, false);
            }
            state.catchFire(world, pos, face, null);
        }
    }

    @Override
    public BlockState updatePostPlacement(BlockState state,
                                          Direction facing,
                                          BlockState facingState,
                                          IWorld world,
                                          BlockPos currentPos,
                                          BlockPos facingPos) {
        return this.isValidPosition(state, world, currentPos) &&
               CapabilityChunkStorage.contains(world.getWorld().getChunkAt(currentPos), EnumStorage.OXYGEN, 1) ?
               this.getStateForPlacement(world, currentPos).with(AGE_0_15, state.get(AGE_0_15)) :
               Blocks.AIR.getDefaultState();
    }
}