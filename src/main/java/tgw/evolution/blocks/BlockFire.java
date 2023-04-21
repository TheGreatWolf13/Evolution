package tgw.evolution.blocks;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.DirectionUtil;

import java.util.Random;
import java.util.random.RandomGenerator;

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

    private static int getNeighborEncouragement(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos)) {
            return 0;
        }
        int i = 0;
        for (Direction direction : DirectionUtil.ALL) {
            BlockState blockstate = level.getBlockState(pos.relative(direction));
            i = Math.max(blockstate.getFlammability(level, pos.relative(direction), DirectionUtil.getOpposite(direction)), i);
        }
        return i;
    }

    private static int getTickCooldown(RandomGenerator random) {
        return 30 + random.nextInt(10);
    }

    public static void init() {
        BlockFire fire = EvolutionBlocks.FIRE.get();
        for (WoodVariant variant : WoodVariant.VALUES) {
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

//    @Override
//    @OnlyIn(Dist.CLIENT)
//    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleManager manager) {
//        return true;
//    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        if (rand.nextInt(8) == 0) {
            level.playLocalSound(pos.getX() + 0.5,
                                 pos.getY() + 0.5,
                                 pos.getZ() + 0.5,
                                 SoundEvents.FIRE_AMBIENT,
                                 SoundSource.BLOCKS,
                                 1.0F + rand.nextFloat(),
                                 rand.nextFloat() * 0.7F + 0.3F,
                                 false);
        }
        BlockPos posDown = pos.below();
        if (!this.canCatchFire(level, posDown, Direction.UP) && !BlockUtils.hasSolidSide(level, posDown, Direction.UP)) {
            if (this.canCatchFire(level, posDown.west(), Direction.EAST)) {
                for (int j = 0; j < 2; ++j) {
                    double x = pos.getX() + rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(level, pos.east(), Direction.WEST)) {
                for (int k = 0; k < 2; ++k) {
                    double x = (pos.getX() + 1) - rand.nextDouble() * 0.1;
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(level, pos.north(), Direction.SOUTH)) {
                for (int l = 0; l < 2; ++l) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = pos.getZ() + rand.nextDouble() * 0.1;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(level, pos.south(), Direction.NORTH)) {
                for (int i1 = 0; i1 < 2; ++i1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = pos.getY() + rand.nextDouble();
                    double z = (pos.getZ() + 1) - rand.nextDouble() * 0.1;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
            if (this.canCatchFire(level, pos.above(), Direction.DOWN)) {
                for (int j1 = 0; j1 < 2; ++j1) {
                    double x = pos.getX() + rand.nextDouble();
                    double y = (pos.getY() + 1) - rand.nextDouble() * 0.1;
                    double z = pos.getZ() + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
                }
            }
        }
        else {
            for (int i = 0; i < 3; ++i) {
                double x = pos.getX() + rand.nextDouble();
                double y = pos.getY() + rand.nextDouble() * 0.5 + 0.5;
                double z = pos.getZ() + rand.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            }
        }
    }

    private boolean areNeighborsFlammable(BlockGetter level, BlockPos pos) {
        for (Direction direction : DirectionUtil.ALL) {
            if (this.canCatchFire(level, pos.relative(direction), DirectionUtil.getOpposite(direction))) {
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
     * @param level The current level
     * @param pos   Block position
     * @param face  The side the fire is coming from
     * @return True if the face can catch fire.
     */
    public boolean canCatchFire(BlockGetter level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFlammable(level, pos, face);
    }

    protected boolean canDie(Level level, BlockPos pos) {
        return level.isRainingAt(pos) ||
               level.isRainingAt(pos.west()) ||
               level.isRainingAt(pos.east()) ||
               level.isRainingAt(pos.north()) ||
               level.isRainingAt(pos.south());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return BlockUtils.hasSolidSide(level, pos.below(), Direction.UP) || this.areNeighborsFlammable(level, pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE_0_15, NORTH, EAST, SOUTH, WEST, UP);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
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
        super.entityInside(state, level, pos, entity);
    }

    public int getActualEncouragement(BlockState state) {
        return this.encouragements.getInt(state.getBlock());
    }

    public int getActualFlammability(BlockState state) {
        return this.flammabilities.getInt(state.getBlock());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.empty();
        if (state.getValue(NORTH)) {
            shape = EvolutionShapes.SLAB_16_N;
        }
        if (state.getValue(SOUTH)) {
            shape = Shapes.join(shape, EvolutionShapes.SLAB_16_S, BooleanOp.OR);
        }
        if (state.getValue(EAST)) {
            shape = Shapes.join(shape, EvolutionShapes.SLAB_16_E, BooleanOp.OR);
        }
        if (state.getValue(WEST)) {
            shape = Shapes.join(shape, EvolutionShapes.SLAB_16_W, BooleanOp.OR);
        }
        if (state.getValue(UP)) {
            shape = Shapes.join(shape, EvolutionShapes.SLAB_16_U, BooleanOp.OR);
        }
        if (shape == Shapes.empty()) {
            return EvolutionShapes.SLAB_16_D[0];
        }
        return shape;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getStateForPlacement(context.getLevel(), context.getClickedPos());
    }

    public BlockState getStateForPlacement(BlockGetter level, BlockPos pos) {
        BlockPos posDown = pos.below();
        if (!this.canCatchFire(level, pos, Direction.UP) && !BlockUtils.hasSolidSide(level, posDown, Direction.UP)) {
            BlockState state = this.defaultBlockState();
            for (Direction direction : DirectionUtil.ALL_EXCEPT_DOWN) {
                BooleanProperty booleanProperty = directionToProperty(direction);
                state = state.setValue(booleanProperty, this.canCatchFire(level, pos.relative(direction), DirectionUtil.getOpposite(direction)));
            }
            return state;
        }
        return this.defaultBlockState();
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
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
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (!state.canSurvive(level, pos)) {
                level.removeBlock(pos, false);
            }
            else {
                level.scheduleTick(pos, this, getTickCooldown(level.random));
                BlockState stateDown = level.getBlockState(pos.below());
                if (stateDown.getBlock() == EvolutionBlocks.PIT_KILN.get() && stateDown.getValue(LAYERS_0_16) == 16) {
                    if (BlockPitKiln.canBurn(level, pos.below())) {
                        TEPitKiln tile = (TEPitKiln) level.getBlockEntity(pos.below());
                        assert tile != null;
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
    public void tick(BlockState state, ServerLevel level, BlockPos pos, Random random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!level.isAreaLoaded(pos, 2)) {
                return;
            }
            if (!state.canSurvive(level, pos)) {
                level.removeBlock(pos, false);
            }
            BlockState stateDown = level.getBlockState(pos.below());
            boolean isDownFireSource = stateDown.isFireSource(level, pos.below(), Direction.UP);
            int age = state.getValue(AGE_0_15);
            if (level.isRaining() && this.canDie(level, pos) && random.nextFloat() < 0.2F + age * 0.03F) {
                level.removeBlock(pos, false);
            }
            else {
                int j = Math.min(15, age + random.nextInt(3) / 2);
                if (age != j) {
                    state = state.setValue(AGE_0_15, j);
                    level.setBlock(pos, state, BlockFlags.NO_RERENDER);
                }
                if (!isDownFireSource) {
                    level.scheduleTick(pos, this, getTickCooldown(level.random));
                    if (!this.areNeighborsFlammable(level, pos)) {
                        if (!BlockUtils.hasSolidSide(level, pos.below(), Direction.UP) || age > 3) {
                            level.removeBlock(pos, false);
                        }
                        return;
                    }
                    if (age == 15 && random.nextInt(4) == 0 && !this.canCatchFire(level, pos.below(), Direction.UP)) {
                        level.removeBlock(pos, false);
                        return;
                    }
                }
                boolean isHighHumidity = level.isHumidAt(pos);
                int humidyModifier = isHighHumidity ? -50 : 0;
                this.tryCatchFire(level, pos.east(), 300 + humidyModifier, random, age, Direction.WEST);
                this.tryCatchFire(level, pos.west(), 300 + humidyModifier, random, age, Direction.EAST);
                this.tryCatchFire(level, pos.below(), 250 + humidyModifier, random, age, Direction.UP);
                this.tryCatchFire(level, pos.above(), 250 + humidyModifier, random, age, Direction.DOWN);
                this.tryCatchFire(level, pos.north(), 300 + humidyModifier, random, age, Direction.SOUTH);
                this.tryCatchFire(level, pos.south(), 300 + humidyModifier, random, age, Direction.NORTH);
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                for (int x = -1; x <= 1; ++x) {
                    for (int z = -1; z <= 1; ++z) {
                        for (int y = -1; y <= 4; ++y) {
                            if (x != 0 || y != 0 || z != 0) {
                                int k1 = 100;
                                if (y > 1) {
                                    k1 += (y - 1) * 100;
                                }
                                mutableBlockPos.set(pos).move(x, y, z);
                                int l1 = getNeighborEncouragement(level, mutableBlockPos);
                                if (l1 > 0) {
                                    int i2 = (l1 + 40 + level.getDifficulty().getId() * 7) / (age + 30);
                                    if (isHighHumidity) {
                                        i2 /= 2;
                                    }
                                    if (i2 > 0 && random.nextInt(k1) <= i2 && (!level.isRaining() || !this.canDie(level, mutableBlockPos))) {
                                        int j2 = Math.min(15, age + random.nextInt(5) / 4);
                                        level.setBlock(mutableBlockPos,
                                                       this.getStateForPlacement(level, mutableBlockPos).setValue(AGE_0_15, j2),
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

    private void tryCatchFire(Level level, BlockPos pos, int chance, RandomGenerator random, int age, Direction face) {
        int i = level.getBlockState(pos).getFlammability(level, pos, face);
        if (random.nextInt(chance) < i) {
            BlockState state = level.getBlockState(pos);
            if (random.nextInt(age + 10) < 5 && !level.isRainingAt(pos)) {
                int j = Math.min(age + random.nextInt(5) / 4, 15);
                level.setBlockAndUpdate(pos, this.getStateForPlacement(level, pos).setValue(AGE_0_15, j));
            }
            else {
                level.removeBlock(pos, false);
            }
            state.onCaughtFire(level, pos, face, null);
        }
    }

    @Override
    public BlockState updateShape(BlockState state,
                                  Direction facing,
                                  BlockState facingState,
                                  LevelAccessor level,
                                  BlockPos currentPos,
                                  BlockPos facingPos) {
        return this.canSurvive(state, level, currentPos) ?
               this.getStateForPlacement(level, currentPos).setValue(AGE_0_15, state.getValue(AGE_0_15)) :
               Blocks.AIR.defaultBlockState();
    }
}