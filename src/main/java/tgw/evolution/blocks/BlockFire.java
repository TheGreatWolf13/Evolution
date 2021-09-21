package tgw.evolution.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.capabilities.chunkstorage.CapabilityChunkStorage;
import tgw.evolution.capabilities.chunkstorage.EnumStorage;
import tgw.evolution.entities.IEntityPatch;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionHitBoxes;
import tgw.evolution.util.BlockFlags;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.WoodVariant;

import javax.annotation.Nullable;
import java.util.Random;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockFire extends BlockGeneric implements IReplaceable, IFireSource {

    public static final SoundType FIRE = new SoundType(0.5f,
                                                       2.6f,
                                                       SoundEvents.FIRE_EXTINGUISH,
                                                       SoundEvents.WOOL_STEP,
                                                       SoundEvents.WOOL_PLACE,
                                                       SoundEvents.WOOL_HIT,
                                                       SoundEvents.WOOL_FALL);
    private final Object2IntMap<Block> encouragements = new Object2IntOpenHashMap<>();
    private final Object2IntMap<Block> flammabilities = new Object2IntOpenHashMap<>();

    public BlockFire() {
        super(Properties.of(Material.FIRE).noCollission().randomTicks().strength(0).lightLevel(state -> 15).sound(FIRE).noDrops());
        this.registerDefaultState(this.defaultBlockState()
                                      .setValue(AGE_0_15, 0)
                                      .setValue(NORTH, false)
                                      .setValue(EAST, false)
                                      .setValue(SOUTH, false)
                                      .setValue(WEST, false)
                                      .setValue(UP, false));
    }

    private static int getNeighborEncouragement(IWorldReader world, BlockPos pos) {
        if (!world.isEmptyBlock(pos)) {
            return 0;
        }
        int i = 0;
        for (Direction direction : Direction.values()) {
            BlockState blockstate = world.getBlockState(pos.relative(direction));
            i = Math.max(blockstate.getFlammability(world, pos.relative(direction), direction.getOpposite()), i);
        }
        return i;
    }

    private static int getTickCooldown(Random random) {
        return 30 + random.nextInt(10);
    }

    public static void init() {
        BlockFire fire = EvolutionBlocks.FIRE.get();
        for (WoodVariant variant : WoodVariant.values()) {
            fire.setFireInfo(variant.getPlanks(), 5, 20);
            fire.setFireInfo(variant.getLog(), 5, 5);
            fire.setFireInfo(variant.getChoppingBlock(), 5, 5);
            fire.setFireInfo(variant.getLeaves(), 30, 60);
        }
        //        fire.setFireInfo(Blocks.OAK_SLAB, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_FENCE, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_STAIRS, 5, 20);
        //        fire.setFireInfo(Blocks.BOOKSHELF, 30, 20);
        //        fire.setFireInfo(Blocks.TNT, 15, 100);
        fire.setFireInfo(EvolutionBlocks.FIREWOOD_PILE.get(), 5, 5);
        fire.setFireInfo(EvolutionBlocks.GRASS.get(), 60, 100);
        fire.setFireInfo(EvolutionBlocks.TALLGRASS.get(), 60, 100);
        //        fire.setFireInfo(Blocks.WHITE_WOOL, 30, 60);
        //        fire.setFireInfo(Blocks.VINE, 15, 100);
        //        fire.setFireInfo(Blocks.COAL_BLOCK, 5, 5);
        //        fire.setFireInfo(Blocks.HAY_BLOCK, 60, 20);
        //        fire.setFireInfo(Blocks.WHITE_CARPET, 60, 20);
        //        fire.setFireInfo(Blocks.DRIED_KELP_BLOCK, 30, 60);
        //        fire.setFireInfo(Blocks.BAMBOO, 60, 60);
        //        fire.setFireInfo(Blocks.SCAFFOLDING, 60, 60);
        //        fire.setFireInfo(Blocks.LECTERN, 30, 20);
        //        fire.setFireInfo(Blocks.COMPOSTER, 5, 20);
        //        fire.setFireInfo(Blocks.SWEET_BERRY_BUSH, 60, 100);
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
            world.playLocalSound(pos.getX() + 0.5,
                                 pos.getY() + 0.5,
                                 pos.getZ() + 0.5,
                                 SoundEvents.FIRE_AMBIENT,
                                 SoundCategory.BLOCKS,
                                 1.0F + rand.nextFloat(),
                                 rand.nextFloat() * 0.7F + 0.3F,
                                 false);
        }
        BlockPos posDown = pos.below();
        if (!this.canCatchFire(world, posDown, Direction.UP) && !BlockUtils.hasSolidSide(world, posDown, Direction.UP)) {
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
            if (this.canCatchFire(world, pos.above(), Direction.DOWN)) {
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
            if (this.canCatchFire(world, pos.relative(direction), direction.getOpposite())) {
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
    public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
        return BlockUtils.hasSolidSide(world, pos.below(), Direction.UP) || this.areNeighborsFlammable(world, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE_0_15, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!entity.fireImmune()) {
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 1);
            if (entity.getRemainingFireTicks() == 0) {
                entity.setSecondsOnFire(8);
            }
            if (((IEntityPatch) entity).getFireDamageImmunity() == 0) {
                entity.hurt(EvolutionDamage.IN_FIRE, 2.5f);
                ((IEntityPatch) entity).setFireDamageImmunity(10);
            }
        }
        super.entityInside(state, world, pos, entity);
    }

    public int getActualEncouragement(BlockState state) {
        return state.hasProperty(FLUID_LOGGED) && state.getValue(FLUID_LOGGED) ? 0 : this.encouragements.getInt(state.getBlock());
    }

    public int getActualFlammability(BlockState state) {
        return state.hasProperty(FLUID_LOGGED) && state.getValue(FLUID_LOGGED) ? 0 : this.flammabilities.getInt(state.getBlock());
    }

    @Override
    public NonNullList<ItemStack> getDrops(World world, BlockPos pos, BlockState state) {
        return NonNullList.of(ItemStack.EMPTY);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        VoxelShape shape = VoxelShapes.empty();
        if (state.getValue(NORTH)) {
            shape = EvolutionHitBoxes.SIXTEENTH_SLAB_NORTH_1;
        }
        if (state.getValue(SOUTH)) {
            shape = VoxelShapes.join(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_SOUTH_1, IBooleanFunction.OR);
        }
        if (state.getValue(EAST)) {
            shape = VoxelShapes.join(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_EAST_1, IBooleanFunction.OR);
        }
        if (state.getValue(WEST)) {
            shape = VoxelShapes.join(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_WEST_1, IBooleanFunction.OR);
        }
        if (state.getValue(UP)) {
            shape = VoxelShapes.join(shape, EvolutionHitBoxes.SIXTEENTH_SLAB_UPPER_1, IBooleanFunction.OR);
        }
        if (shape == VoxelShapes.empty()) {
            return EvolutionHitBoxes.SIXTEENTH_SLAB_LOWER_1;
        }
        return shape;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(IBlockReader world, BlockPos pos) {
        BlockPos posDown = pos.below();
        if (!this.canCatchFire(world, pos, Direction.UP) && !BlockUtils.hasSolidSide(world, posDown, Direction.UP)) {
            BlockState state = this.defaultBlockState();
            for (Direction direction : MathHelper.DIRECTIONS_EXCEPT_DOWN) {
                BooleanProperty booleanProperty = directionToProperty(direction);
                if (booleanProperty != null) {
                    state = state.setValue(booleanProperty, this.canCatchFire(world, pos.relative(direction), direction.getOpposite()));
                }
            }
            return state;
        }
        return this.defaultBlockState();
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
    public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (!state.canSurvive(world, pos)) {
                world.removeBlock(pos, false);
            }
            else {
                world.getBlockTicks().scheduleTick(pos, this, getTickCooldown(world.random));
                BlockState stateDown = world.getBlockState(pos.below());
                if (stateDown.getBlock() == EvolutionBlocks.PIT_KILN.get() && stateDown.getValue(LAYERS_0_16) == 16) {
                    if (BlockPitKiln.canBurn(world, pos.below())) {
                        TEPitKiln tile = (TEPitKiln) world.getBlockEntity(pos.below());
                        tile.start();
                    }
                }
            }
        }
    }

    public void setFireInfo(Block block, int encouragement, int flammability) {
        if (block == Blocks.AIR) {
            throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        }
        this.encouragements.put(block, encouragement);
        this.flammabilities.put(block, flammability);
    }

    @Override
    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (world.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!world.isAreaLoaded(pos, 2)) {
                return;
            }
            if (!state.canSurvive(world, pos)) {
                world.removeBlock(pos, false);
            }
            BlockState stateDown = world.getBlockState(pos.below());
            boolean isDownFireSource = stateDown.isFireSource(world, pos.below(), Direction.UP);
            int age = state.getValue(AGE_0_15);
            if (world.isRaining() && this.canDie(world, pos) && random.nextFloat() < 0.2F + age * 0.03F) {
                world.removeBlock(pos, false);
            }
            else {
                int j = Math.min(15, age + random.nextInt(3) / 2);
                if (age != j) {
                    state = state.setValue(AGE_0_15, j);
                    world.setBlock(pos, state, BlockFlags.NO_RERENDER);
                }
                if (!isDownFireSource) {
                    world.getBlockTicks().scheduleTick(pos, this, getTickCooldown(world.random));
                    if (!this.areNeighborsFlammable(world, pos)) {
                        if (!BlockUtils.hasSolidSide(world, pos.below(), Direction.UP) || age > 3) {
                            world.removeBlock(pos, false);
                        }
                        return;
                    }
                    if (age == 15 && random.nextInt(4) == 0 && !this.canCatchFire(world, pos.below(), Direction.UP)) {
                        world.removeBlock(pos, false);
                        return;
                    }
                }
                boolean isHighHumidity = world.isHumidAt(pos);
                int humidyModifier = isHighHumidity ? -50 : 0;
                this.tryCatchFire(world, pos.east(), 300 + humidyModifier, random, age, Direction.WEST);
                this.tryCatchFire(world, pos.west(), 300 + humidyModifier, random, age, Direction.EAST);
                this.tryCatchFire(world, pos.below(), 250 + humidyModifier, random, age, Direction.UP);
                this.tryCatchFire(world, pos.above(), 250 + humidyModifier, random, age, Direction.DOWN);
                this.tryCatchFire(world, pos.north(), 300 + humidyModifier, random, age, Direction.SOUTH);
                this.tryCatchFire(world, pos.south(), 300 + humidyModifier, random, age, Direction.NORTH);
                BlockPos.Mutable mutableBlockPos = new BlockPos.Mutable();
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = -1; y <= 4; ++y) {
                            if (x != 0 || y != 0 || z != 0) {
                                int k1 = 100;
                                if (y > 1) {
                                    k1 += (y - 1) * 100;
                                }
                                mutableBlockPos.set(pos).move(x, y, z);
                                int l1 = getNeighborEncouragement(world, mutableBlockPos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + world.getDifficulty().getId() * 7) / (age + 30);
                                    if (isHighHumidity) {
                                        i2 /= 2;
                                    }
                                    if (i2 > 0 && random.nextInt(k1) <= i2 && (!world.isRaining() || !this.canDie(world, mutableBlockPos))) {
                                        int j2 = Math.min(15, age + random.nextInt(5) / 4);
                                        world.setBlock(mutableBlockPos,
                                                       this.getStateForPlacement(world, mutableBlockPos).setValue(AGE_0_15, j2),
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

    private void tryCatchFire(World world, BlockPos pos, int chance, Random random, int age, Direction face) {
        int i = world.getBlockState(pos).getFlammability(world, pos, face);
        if (random.nextInt(chance) < i) {
            BlockState state = world.getBlockState(pos);
            if (random.nextInt(age + 10) < 5 && !world.isRainingAt(pos)) {
                int j = Math.min(age + random.nextInt(5) / 4, 15);
                world.setBlockAndUpdate(pos, this.getStateForPlacement(world, pos).setValue(AGE_0_15, j));
            }
            else {
                world.removeBlock(pos, false);
            }
            state.catchFire(world, pos, face, null);
        }
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
        return this.canSurvive(state, world, currentPos) &&
               CapabilityChunkStorage.contains(world.getChunkSource().getChunk(currentPos.getX() >> 4, currentPos.getZ() >> 4, false),
                                               EnumStorage.OXYGEN,
                                               1) ?
               this.getStateForPlacement(world, currentPos).setValue(AGE_0_15, state.getValue(AGE_0_15)) :
               Blocks.AIR.defaultBlockState();
    }
}