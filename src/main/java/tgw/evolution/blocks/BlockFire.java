package tgw.evolution.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.capabilities.chunkstorage.ChunkStorageCapability;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Random;

public class BlockFire extends Block implements IReplaceable {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_0_15;
    public static final BooleanProperty NORTH = SixWayBlock.NORTH;
    public static final BooleanProperty EAST = SixWayBlock.EAST;
    public static final BooleanProperty SOUTH = SixWayBlock.SOUTH;
    public static final BooleanProperty WEST = SixWayBlock.WEST;
    public static final BooleanProperty UP = SixWayBlock.UP;
    public static final SoundType FIRE = new SoundType(0.5f,
                                                       2.6f,
                                                       SoundEvents.BLOCK_FIRE_EXTINGUISH,
                                                       SoundEvents.BLOCK_WOOL_STEP,
                                                       SoundEvents.BLOCK_WOOL_PLACE,
                                                       SoundEvents.BLOCK_WOOL_HIT,
                                                       SoundEvents.BLOCK_WOOL_FALL);
    private static final VoxelShape SHAPE_UP = EvolutionHitBoxes.SIXTEENTH_SLAB_UPPER_1;
    private static final VoxelShape SHAPE_DOWN = EvolutionHitBoxes.SIXTEENTH_SLAB_LOWER_1;
    private static final VoxelShape SHAPE_WEST = EvolutionHitBoxes.SIXTEENTH_SLAB_WEST_1;
    private static final VoxelShape SHAPE_EAST = EvolutionHitBoxes.SIXTEENTH_SLAB_EAST_1;
    private static final VoxelShape SHAPE_NORTH = EvolutionHitBoxes.SIXTEENTH_SLAB_NORTH_1;
    private static final VoxelShape SHAPE_SOUTH = EvolutionHitBoxes.SIXTEENTH_SLAB_SOUTH_1;
    private static final Map<Direction, BooleanProperty> FACING_TO_PROPERTY_MAP = SixWayBlock.FACING_TO_PROPERTY_MAP.entrySet()
                                                                                                                    .stream()
                                                                                                                    .filter(entry -> entry.getKey() != Direction.DOWN)
                                                                                                                    .collect(Util.toMapCollector());
    private final Object2IntMap<Block> encouragements = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Block> flammabilities = new Object2IntOpenHashMap<>();
    public BlockFire() {
        super(Properties.create(Material.FIRE).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0).lightValue(15).sound(FIRE).noDrops());
        this.setDefaultState(this.getDefaultState()
                                 .with(AGE, 0)
                                 .with(NORTH, false)
                                 .with(EAST, false)
                                 .with(SOUTH, false)
                                 .with(WEST, false)
                                 .with(UP, false));
    }

    public static void init() {
        BlockFire fireblock = (BlockFire) EvolutionBlocks.FIRE.get();
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
    public boolean canBeReplacedByLiquid(BlockState state) {
        return true;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = VoxelShapes.empty();
        if (state.get(NORTH)) {
            shape = SHAPE_NORTH;
        }
        if (state.get(SOUTH)) {
            shape = VoxelShapes.combine(shape, SHAPE_SOUTH, IBooleanFunction.OR);
        }
        if (state.get(EAST)) {
            shape = VoxelShapes.combine(shape, SHAPE_EAST, IBooleanFunction.OR);
        }
        if (state.get(WEST)) {
            shape = VoxelShapes.combine(shape, SHAPE_WEST, IBooleanFunction.OR);
        }
        if (state.get(UP)) {
            shape = VoxelShapes.combine(shape, SHAPE_UP, IBooleanFunction.OR);
        }
        if (shape == VoxelShapes.empty()) {
            return SHAPE_DOWN;
        }
        return shape;
    }

    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager manager) {
        return true;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        return this.isValidPosition(stateIn, worldIn, currentPos) && ChunkStorageCapability.contains(worldIn.getWorld().getChunkAt(currentPos),
                                                                                                     EnumStorage.OXYGEN,
                                                                                                     1) ? this.getStateForPlacement(worldIn,
                                                                                                                                    currentPos)
                                                                                                              .with(AGE,
                                                                                                                    stateIn.get(AGE)) : Blocks.AIR.getDefaultState();
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
            for (Direction direction : Direction.values()) {
                BooleanProperty booleanProperty = FACING_TO_PROPERTY_MAP.get(direction);
                if (booleanProperty != null) {
                    state = state.with(booleanProperty, this.canCatchFire(worldIn, pos.offset(direction), direction.getOpposite()));
                }
            }
            return state;
        }
        return this.getDefaultState();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        return worldIn.getBlockState(blockpos).func_224755_d(worldIn, blockpos, Direction.UP) || this.areNeighborsFlammable(worldIn, pos);
    }

    @Override
    public int tickRate(IWorldReader worldIn) {
        return 30;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (worldIn.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
            if (!worldIn.isAreaLoaded(pos, 2)) {
                return;
            }
            if (!state.isValidPosition(worldIn, pos)) {
                worldIn.removeBlock(pos, false);
            }
            BlockState stateDown = worldIn.getBlockState(pos.down());
            boolean isDownFireSource = stateDown.isFireSource(worldIn, pos.down(), Direction.UP);
            int age = state.get(AGE);
            if (worldIn.isRaining() && this.canDie(worldIn, pos) && random.nextFloat() < 0.2F + age * 0.03F) {
                worldIn.removeBlock(pos, false);
            }
            else {
                int j = Math.min(15, age + random.nextInt(3) / 2);
                if (age != j) {
                    state = state.with(AGE, j);
                    worldIn.setBlockState(pos, state, 4);
                }
                if (!isDownFireSource) {
                    worldIn.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(worldIn) + random.nextInt(10));
                    if (!this.areNeighborsFlammable(worldIn, pos)) {
                        BlockPos blockpos = pos.down();
                        if (!worldIn.getBlockState(blockpos).func_224755_d(worldIn, blockpos, Direction.UP) || age > 3) {
                            worldIn.removeBlock(pos, false);
                        }
                        return;
                    }
                    if (age == 15 && random.nextInt(4) == 0 && !this.canCatchFire(worldIn, pos.down(), Direction.UP)) {
                        worldIn.removeBlock(pos, false);
                        return;
                    }
                }
                boolean isHighHumidity = worldIn.isBlockinHighHumidity(pos);
                int humidyModifier = isHighHumidity ? -50 : 0;
                this.tryCatchFire(worldIn, pos.east(), 300 + humidyModifier, random, age, Direction.WEST);
                this.tryCatchFire(worldIn, pos.west(), 300 + humidyModifier, random, age, Direction.EAST);
                this.tryCatchFire(worldIn, pos.down(), 250 + humidyModifier, random, age, Direction.UP);
                this.tryCatchFire(worldIn, pos.up(), 250 + humidyModifier, random, age, Direction.DOWN);
                this.tryCatchFire(worldIn, pos.north(), 300 + humidyModifier, random, age, Direction.SOUTH);
                this.tryCatchFire(worldIn, pos.south(), 300 + humidyModifier, random, age, Direction.NORTH);
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
                                int l1 = this.getNeighborEncouragement(worldIn, mutableBlockPos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + worldIn.getDifficulty().getId() * 7) / (age + 30);
                                    if (isHighHumidity) {
                                        i2 /= 2;
                                    }
                                    if (i2 > 0 && random.nextInt(k1) <= i2 && (!worldIn.isRaining() || !this.canDie(worldIn, mutableBlockPos))) {
                                        int j2 = Math.min(15, age + random.nextInt(5) / 4);
                                        worldIn.setBlockState(mutableBlockPos, this.getStateForPlacement(worldIn, mutableBlockPos).with(AGE, j2), 3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected boolean canDie(World worldIn, BlockPos pos) {
        return worldIn.isRainingAt(pos) || worldIn.isRainingAt(pos.west()) || worldIn.isRainingAt(pos.east()) || worldIn.isRainingAt(pos.north()) || worldIn
                .isRainingAt(pos.south());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (player.getHeldItem(handIn).getItem() == EvolutionItems.stick.get()) {
            if (!worldIn.isRemote) {
                player.getHeldItem(handIn).shrink(1);
                ItemStack torch = new ItemStack(EvolutionItems.torch.get());
                if (!player.inventory.addItemStackToInventory(torch)) {
                    BlockUtils.dropItemStack(worldIn, player.getPosition(), torch);
                }
            }
            return true;
        }
        return false;
    }

    public int getActualFlammability(BlockState state) {
        return state.has(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED) ? 0 : this.flammabilities.getInt(state.getBlock());
    }

    public int getActualEncouragement(BlockState state) {
        return state.has(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED) ? 0 : this.encouragements.getInt(state.getBlock());
    }

    private void tryCatchFire(World worldIn, BlockPos pos, int chance, Random random, int age, Direction face) {
        int i = worldIn.getBlockState(pos).getFlammability(worldIn, pos, face);
        if (random.nextInt(chance) < i) {
            BlockState state = worldIn.getBlockState(pos);
            if (random.nextInt(age + 10) < 5 && !worldIn.isRainingAt(pos)) {
                int j = Math.min(age + random.nextInt(5) / 4, 15);
                worldIn.setBlockState(pos, this.getStateForPlacement(worldIn, pos).with(AGE, j), 3);
            }
            else {
                worldIn.removeBlock(pos, false);
            }
            state.catchFire(worldIn, pos, face, null);
        }
    }

    private boolean areNeighborsFlammable(IBlockReader worldIn, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (this.canCatchFire(worldIn, pos.offset(direction), direction.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private int getNeighborEncouragement(IWorldReader worldIn, BlockPos pos) {
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

    @Deprecated //Forge: Use canCatchFire with more context
    public boolean canBurn(BlockState state) {
        return this.getActualEncouragement(state) > 0;
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
                if (stateDown.getBlock() == EvolutionBlocks.PIT_KILN.get() && stateDown.get(EvolutionBlockStateProperties.LAYERS_0_16) == 16) {
                    if (BlockPitKiln.canBurn(worldIn.getWorld(), pos.down())) {
                        TEPitKiln tile = (TEPitKiln) worldIn.getTileEntity(pos.down());
                        tile.start();
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(24) == 0) {
            worldIn.playSound(pos.getX() + 0.5,
                              pos.getY() + 0.5,
                              pos.getZ() + 0.5,
                              SoundEvents.BLOCK_FIRE_AMBIENT,
                              SoundCategory.BLOCKS,
                              1.0F + rand.nextFloat(),
                              rand.nextFloat() * 0.7F + 0.3F,
                              false);
        }
        BlockPos posDown = pos.down();
        BlockState stateDown = worldIn.getBlockState(posDown);
        if (!this.canCatchFire(worldIn, posDown, Direction.UP) && !Block.hasSolidSide(stateDown, worldIn, posDown, Direction.UP)) {
            if (this.canCatchFire(worldIn, posDown.west(), Direction.EAST)) {
                for (int j = 0; j < 2; ++j) {
                    double x = pos.getX() + rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(worldIn, pos.east(), Direction.WEST)) {
                for (int k = 0; k < 2; ++k) {
                    double x = (pos.getX() + 1) - rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(worldIn, pos.north(), Direction.SOUTH)) {
                for (int l = 0; l < 2; ++l) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble() * 0.1;
                    worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(worldIn, pos.south(), Direction.NORTH)) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = (pos.getZ() + 1) - rand.nextDouble() * 0.1;
                    worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(worldIn, pos.up(), Direction.DOWN)) {
                for (int j1 = 0; j1 < 2; ++j1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = (pos.getY() + 1) - rand.nextDouble() * 0.1;
                    double z = pos.getZ() + rand.nextDouble();
                    worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
        }
        else {
            for (int i = 0; i < 3; ++i) {
                double x = pos.getX() + rand.nextDouble();
                double y = pos.getY() + rand.nextDouble() * 0.5 + 0.5;
                double z = pos.getZ() + rand.nextDouble();
                worldIn.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            }
        }
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE, NORTH, EAST, SOUTH, WEST, UP);
    }

    public void setFireInfo(RegistryObject<Block> blockIn, int encouragement, int flammability) {
        if (blockIn.get() == Blocks.AIR) {
            throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        }
        this.encouragements.put(blockIn.get(), encouragement);
        this.flammabilities.put(blockIn.get(), flammability);
    }

    @Override
    public boolean isBurning(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
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

    @Override
    public ItemStack getDrops(BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }
}