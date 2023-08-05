package tgw.evolution.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.blocks.tileentities.TEPitKiln;
import tgw.evolution.blocks.util.BlockUtils;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionDamage;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.collection.maps.R2IHashMap;
import tgw.evolution.util.collection.maps.R2IMap;
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
    private final R2IMap<Block> burnOdds = new R2IHashMap<>();
    private final R2IMap<Block> flameOdds = new R2IHashMap<>();

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

    private static int getTickCooldown(RandomGenerator random) {
        return 30 + random.nextInt(10);
    }

    public static void init() {
        BlockFire fire = EvolutionBlocks.FIRE;
        for (WoodVariant variant : WoodVariant.VALUES) {
            fire.setFireInfo(variant.get(EvolutionBlocks.PLANKS), 5, 20);
            fire.setFireInfo(variant.get(EvolutionBlocks.LOGS), 5, 5);
            fire.setFireInfo(variant.get(EvolutionBlocks.CHOPPING_BLOCKS), 5, 5);
            fire.setFireInfo(variant.get(EvolutionBlocks.LEAVES), 30, 60);
        }
        //        fire.setFireInfo(Blocks.OAK_SLAB, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_FENCE_GATE, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_FENCE, 5, 20);
        //        fire.setFireInfo(Blocks.OAK_STAIRS, 5, 20);
        //        fire.setFireInfo(Blocks.BOOKSHELF, 30, 20);
        //        fire.setFireInfo(Blocks.TNT, 15, 100);
        fire.setFireInfo(EvolutionBlocks.FIREWOOD_PILE, 5, 5);
        fire.setFireInfo(EvolutionBlocks.TALLGRASS, 60, 100);
        fire.setFireInfo(EvolutionBlocks.TALLGRASS_HIGH, 60, 100);
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
    public void animateTick(BlockState state, Level level, BlockPos pos, Random rand) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (rand.nextInt(8) == 0) {
            level.playLocalSound(x + 0.5,
                                 y + 0.5,
                                 z + 0.5,
                                 SoundEvents.FIRE_AMBIENT,
                                 SoundSource.BLOCKS,
                                 1.0F + rand.nextFloat(),
                                 rand.nextFloat() * 0.7F + 0.3F,
                                 false);
        }
        if (!this.canBurn(level.getBlockState_(x, y - 1, z)) && !BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP)) {
            if (this.canBurn(level.getBlockState_(x - 1, y, z))) {
                for (int i = 0; i < 2; ++i) {
                    double dx = x + rand.nextDouble() * 0.1;
                    double dy = y + rand.nextDouble();
                    double dz = z + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x + 1, y, z))) {
                for (int i = 0; i < 2; ++i) {
                    double dx = x + 1 - rand.nextDouble() * 0.1;
                    double dy = y + rand.nextDouble();
                    double dz = z + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y, z - 1))) {
                for (int i = 0; i < 2; ++i) {
                    double dx = x + rand.nextDouble();
                    double dy = y + rand.nextDouble();
                    double dz = z + rand.nextDouble() * 0.1;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y, z + 1))) {
                for (int i = 0; i < 2; ++i) {
                    double dx = x + rand.nextDouble();
                    double dy = y + rand.nextDouble();
                    double dz = z + 1 - rand.nextDouble() * 0.1;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
                }
            }
            if (this.canBurn(level.getBlockState_(x, y + 1, z))) {
                for (int i = 0; i < 2; ++i) {
                    double dx = x + rand.nextDouble();
                    double dy = y + 1 - rand.nextDouble() * 0.1;
                    double dz = z + rand.nextDouble();
                    level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
                }
            }
        }
        else {
            for (int i = 0; i < 3; ++i) {
                double dx = x + rand.nextDouble();
                double dy = y + rand.nextDouble() * 0.5 + 0.5;
                double dz = z + rand.nextDouble();
                level.addParticle(ParticleTypes.LARGE_SMOKE, dx, dy, dz, 0, 0, 0);
            }
        }
    }

    @Override
    public boolean canBeReplacedByFluid(BlockState state) {
        return true;
    }

    @Override
    public boolean canBeReplacedByRope(BlockState state) {
        return true;
    }

    public boolean canBurn(BlockState state) {
        return this.flameOdds.getInt(state.getBlock()) > 0;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        return BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP) || this.isValidFireLocation(level, x, y, z);
    }

    private void checkBurnOut(Level level, int x, int y, int z, int chance, RandomGenerator random, int age) {
        BlockState state = level.getBlockState_(x, y, z);
        int odds = this.getBurnOdds(state);
        if (random.nextInt(chance) < odds) {
            if (random.nextInt(age + 10) < 5 /*&& !level.isRainingAt(pos)*/) {
                int j = Math.min(age + random.nextInt(5) / 4, 15);
                level.setBlockAndUpdate_(x, y, z, this.getStateWithAge(level, x, y, z).setValue(AGE_0_15, j));
            }
            else {
                level.removeBlock_(x, y, z, false);
            }
//            Block block = state.getBlock();
//            if (block instanceof TntBlock) {
//                TntBlock.explode(level, pos);
//            }
        }
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
            entity.hurt(EvolutionDamage.IN_FIRE, 2.5f);
        }
        super.entityInside(state, level, pos, entity);
    }

    public int getBurnOdds(BlockState state) {
        return this.burnOdds.getInt(state.getBlock());
    }

    private int getFireOdds(LevelReader level, int x, int y, int z) {
        if (!level.isEmptyBlock_(x, y, z)) {
            return 0;
        }
        int i = 0;
        for (Direction direction : DirectionUtil.ALL) {
            BlockState blockstate = level.getBlockStateAtSide(x, y, z, direction);
            i = Math.max(this.getBurnOdds(blockstate), i);
        }
        return i;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
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
    public @Nullable BlockState getStateForPlacement_(Level level,
                                                      int x,
                                                      int y,
                                                      int z,
                                                      Player player,
                                                      InteractionHand hand,
                                                      BlockHitResult hitResult) {
        return this.getStateWithAge(level, x, y, z);
    }

    public BlockState getStateWithAge(BlockGetter level, int x, int y, int z) {
        if (!this.canBurn(level.getBlockState_(x, y, z)) && !BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP)) {
            BlockState state = this.defaultBlockState();
            for (Direction direction : DirectionUtil.ALL_EXCEPT_DOWN) {
                BooleanProperty booleanProperty = directionToProperty(direction);
                state = state.setValue(booleanProperty, this.canBurn(level.getBlockStateAtSide(x, y, z, direction)));
            }
            return state;
        }
        return this.defaultBlockState();
    }

    @Override
    public boolean isFireSource(BlockState state) {
        return true;
    }

    protected boolean isNearRain(Level level, int x, int y, int z) {
        return /*level.isRainingAt(pos) ||
               level.isRainingAt(pos.west()) ||
               level.isRainingAt(pos.east()) ||
               level.isRainingAt(pos.north()) ||
               level.isRainingAt(pos.south())*/false;
    }

    @Override
    public boolean isReplaceable(BlockState state) {
        return true;
    }

    private boolean isValidFireLocation(BlockGetter level, int x, int y, int z) {
        for (Direction direction : DirectionUtil.ALL) {
            if (this.canBurn(level.getBlockStateAtSide(x, y, z, direction))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPlace_(BlockState state, Level level, int x, int y, int z, BlockState oldState, boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (!state.canSurvive_(level, x, y, z)) {
                level.removeBlock(new BlockPos(x, y, z), false);
            }
            else {
                level.scheduleTick(new BlockPos(x, y, z), this, getTickCooldown(level.random));
                BlockState stateDown = level.getBlockState_(x, y - 1, z);
                if (stateDown.getBlock() == EvolutionBlocks.PIT_KILN && stateDown.getValue(LAYERS_0_16) == 16) {
                    if (BlockPitKiln.canBurn(level, x, y - 1, z)) {
                        if (level.getBlockEntity_(x, y - 1, z) instanceof TEPitKiln tile) {
                            tile.start();
                        }
                    }
                }
            }
        }
    }

    public void setFireInfo(Block block, int encouragement, int flammability) {
        if (block == Blocks.AIR) {
            throw new IllegalArgumentException("Tried to set air on fire... This is bad.");
        }
        this.flameOdds.put(block, encouragement);
        this.burnOdds.put(block, flammability);
    }

    @Override
    public void tick_(BlockState state, ServerLevel level, int x, int y, int z, Random random) {
        if (level.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            if (!state.canSurvive_(level, x, y, z)) {
                level.removeBlock_(x, y, z, false);
            }
            int age = state.getValue(AGE_0_15);
//            if (level.isRaining() && this.isNearRain(level, pos) && random.nextFloat() < 0.2F + age * 0.03F) {
//                level.removeBlock(pos, false);
//            }
//            else {
            int j = Math.min(15, age + random.nextInt(3) / 2);
            if (age != j) {
                state = state.setValue(AGE_0_15, j);
                level.setBlock_(x, y, z, state, BlockFlags.NO_RERENDER);
            }
            BlockState stateDown = level.getBlockState_(x, y - 1, z);
            if (!stateDown.getBlock().isFireSource(stateDown, level, x, y - 1, z, Direction.UP)) {
                BlockUtils.schedulePreciseBlockTick(level, x, y, z, getTickCooldown(level.random));
                if (!this.isValidFireLocation(level, x, y, z)) {
                    if (!BlockUtils.hasSolidFace(level, x, y - 1, z, Direction.UP) || age > 3) {
                        level.removeBlock_(x, y, z, false);
                    }
                    return;
                }
                if (age == 15 && random.nextInt(4) == 0 && !this.canBurn(level.getBlockState_(x, y - 1, z))) {
                    level.removeBlock_(x, y, z, false);
                    return;
                }
            }
            boolean isHighHumidity = level.isHumidAt_(x, y, z);
            int humidyModifier = isHighHumidity ? -50 : 0;
            this.checkBurnOut(level, x - 1, y, z, 300 + humidyModifier, random, age);
            this.checkBurnOut(level, x + 1, y, z, 300 + humidyModifier, random, age);
            this.checkBurnOut(level, x, y - 1, z, 250 + humidyModifier, random, age);
            this.checkBurnOut(level, x, y + 1, z, 250 + humidyModifier, random, age);
            this.checkBurnOut(level, x, y, z - 1, 300 + humidyModifier, random, age);
            this.checkBurnOut(level, x, y, z + 1, 300 + humidyModifier, random, age);
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    for (int dy = -1; dy <= 4; ++dy) {
                        if (dx != 0 || dy != 0 || dz != 0) {
                            int k1 = 100;
                            if (dy > 1) {
                                k1 += (dy - 1) * 100;
                            }
                            int fireOdds = this.getFireOdds(level, x + dx, y + dy, z + dz);
                            if (fireOdds > 0) {
                                int i2 = (fireOdds + 40 + level.getDifficulty().getId() * 7) / (age + 30);
                                if (isHighHumidity) {
                                    i2 /= 2;
                                }
                                if (i2 > 0 && random.nextInt(k1) <= i2 && (!level.isRaining() || !this.isNearRain(level, x + dx, y + dy, z + dz))) {
                                    int j2 = Math.min(15, age + random.nextInt(5) / 4);
                                    level.setBlock_(x + dx, y + dy, z + dz,
                                                    this.getStateWithAge(level, x + dx, y + dy, z + dz)
                                                        .setValue(AGE_0_15, j2),
                                                    BlockFlags.NOTIFY | BlockFlags.BLOCK_UPDATE);
                                }
                            }
                        }
                    }
                }
            }
//            }
        }
    }

    @Override
    public BlockState updateShape_(BlockState state,
                                   Direction from,
                                   BlockState fromState,
                                   LevelAccessor level,
                                   int x,
                                   int y,
                                   int z,
                                   int fromX,
                                   int fromY,
                                   int fromZ) {
        return this.canSurvive_(state, level, x, y, z) ?
               this.getStateWithAge(level, x, y, z).setValue(AGE_0_15, state.getValue(AGE_0_15)) :
               Blocks.AIR.defaultBlockState();
    }
}