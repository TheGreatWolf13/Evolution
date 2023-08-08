package tgw.evolution.mixin;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchFlowingFluid;
import tgw.evolution.util.collection.TriKey2BLinkedHashCache;
import tgw.evolution.util.constants.BlockFlags;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.VectorUtil;

import java.util.Map;

@Mixin(FlowingFluid.class)
public abstract class Mixin_FMS_FlowingFluid extends Fluid implements PatchFlowingFluid {

    @Mutable @Shadow @Final @RestoreFinal public static BooleanProperty FALLING;
    @Mutable @Shadow @Final @RestoreFinal public static IntegerProperty LEVEL;
    @Unique @RestoreFinal private static ThreadLocal<TriKey2BLinkedHashCache<BlockState, BlockState, Direction>> CACHE;
    @Shadow @Final @DeleteField private static ThreadLocal<Object2ByteLinkedOpenHashMap<Block.BlockStatePairKey>> OCCLUSION_CACHE;
    @Shadow @Final private Map<FluidState, VoxelShape> shapes;

    @Unique
    private static boolean canHoldFluid(BlockGetter level, int x, int y, int z, BlockState state, Fluid fluid) {
        Block block = state.getBlock();
        if (block instanceof LiquidBlockContainer container) {
            return container.canPlaceLiquid(level, new BlockPos(x, y, z), state, fluid);
        }
        if (!(block instanceof DoorBlock) && !state.is(BlockTags.SIGNS) && !state.is(Blocks.LADDER) && !state.is(Blocks.SUGAR_CANE) && !state.is(Blocks.BUBBLE_COLUMN)) {
            Material material = state.getMaterial();
            if (material != Material.PORTAL && material != Material.STRUCTURAL_AIR && material != Material.WATER_PLANT && material != Material.REPLACEABLE_WATER_PLANT) {
                return !material.blocksMotion();
            }
            return false;
        }
        return false;
    }

    @Unique
    private static boolean canPassThroughWall(Direction direction, BlockGetter level, int x, int y, int z, BlockState state, int offX, int offY, int offZ, BlockState stateAtOff) {
        TriKey2BLinkedHashCache<BlockState, BlockState, Direction> cache;
        if (!state.getBlock().hasDynamicShape() && !stateAtOff.getBlock().hasDynamicShape()) {
            cache = CACHE.get();
            byte b = cache.getAndMoveToFirst(state, stateAtOff, direction);
            if (b != 127) {
                return b != 0;
            }
        }
        else {
            cache = null;
        }
        VoxelShape shape = state.getCollisionShape_(level, x, y, z);
        VoxelShape shapeAtOff = stateAtOff.getCollisionShape_(level, offX, offY, offZ);
        boolean notOcclude = !Shapes.mergedFaceOccludes(shape, shapeAtOff, direction);
        if (cache != null) {
            cache.putAndMoveToFirst(state, stateAtOff, direction, (byte) (notOcclude ? 1 : 0));
        }
        return notOcclude;
    }

    @Unique
    private static boolean canSpreadTo(BlockGetter level, int x, int y, int z, BlockState state, Direction direction, int offX, int offY, int offZ, BlockState stateAtOff, FluidState fluidAtOff, Fluid fluidThatWantsToBe) {
        return fluidAtOff.canBeReplacedWith_(level, offX, offY, offZ, fluidThatWantsToBe, direction) && canPassThroughWall(direction, level, x, y, z, state, offX, offY, offZ, stateAtOff) && canHoldFluid(level, offX, offY, offZ, stateAtOff, fluidThatWantsToBe);
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        FALLING = BlockStateProperties.FALLING;
        LEVEL = BlockStateProperties.LEVEL_FLOWING;
        CACHE = ThreadLocal.withInitial(() -> new TriKey2BLinkedHashCache<>(200, (byte) 127));
    }

    @Unique
    private static short getCacheKey(int x, int z, int offX, int offZ) {
        int dx = offX - x;
        int dz = offZ - z;
        return (short) ((dx + 128 & 255) << 8 | dz + 128 & 255);
    }

    @Overwrite
    @DeleteMethod
    private static short getCacheKey(BlockPos blockPos, BlockPos blockPos2) {
        throw new AbstractMethodError();
    }

    @Unique
    private static boolean hasSameAbove_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return fluidState.getType().isSame(level.getFluidState_(x, y + 1, z).getType());
    }

    @Shadow
    protected abstract boolean affectsFlow(FluidState pState);

    @Override
    public void beforeDestroyingBlock_(LevelAccessor level, int x, int y, int z, BlockState state) {
    }

    @Shadow
    protected abstract boolean canConvertToSource();

    @Overwrite
    @DeleteMethod
    private boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    private boolean canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        throw new AbstractMethodError();
    }

    @Unique
    private boolean canPassThrough(BlockGetter level, Fluid newLiquid, int x, int y, int z, BlockState state, Direction direction, int offX, int offY, int offZ, BlockState stateAtOff, FluidState fluidAtOff) {
        return !this.isSourceBlockOfThisType(fluidAtOff) && canPassThroughWall(direction, level, x, y, z, state, offX, offY, offZ, stateAtOff) && canHoldFluid(level, offX, offY, offZ, stateAtOff, newLiquid);
    }

    @Overwrite
    @DeleteMethod
    private boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        throw new AbstractMethodError();
    }

    @Overwrite
    @DeleteMethod
    public boolean canSpreadTo(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState, Fluid fluid) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract int getDropOff(LevelReader levelReader);

    @Override
    public Vec3d getFlow(BlockGetter level, int x, int y, int z, FluidState state, Vec3d flow) {
        double flowX = 0;
        double flowZ = 0;
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            int offX = x + dir.getStepX();
            int offZ = z + dir.getStepZ();
            FluidState fluidAtSide = level.getFluidState_(offX, y, offZ);
            if (this.affectsFlow(fluidAtSide)) {
                float ownHeightAtSide = fluidAtSide.getOwnHeight();
                float dHeight = 0.0F;
                if (ownHeightAtSide == 0.0F) {
                    if (!level.getBlockState_(offX, y, offZ).getMaterial().blocksMotion()) {
                        FluidState fluidAtSideBelow = level.getFluidState_(offX, y - 1, offZ);
                        if (this.affectsFlow(fluidAtSideBelow)) {
                            ownHeightAtSide = fluidAtSideBelow.getOwnHeight();
                            if (ownHeightAtSide > 0.0F) {
                                dHeight = state.getOwnHeight() - (ownHeightAtSide - 0.888_888_9F);
                            }
                        }
                    }
                }
                else if (ownHeightAtSide > 0.0F) {
                    dHeight = state.getOwnHeight() - ownHeightAtSide;
                }
                if (dHeight != 0.0F) {
                    flowX += dir.getStepX() * dHeight;
                    flowZ += dir.getStepZ() * dHeight;
                }
            }
        }
        double flowY = 0;
        if (state.getValue(FALLING)) {
            for (Direction dir : DirectionUtil.HORIZ_NESW) {
                int offX = x + dir.getStepX();
                int offZ = z + dir.getStepZ();
                if (this.isSolidFace(level, offX, y, offZ, dir) || this.isSolidFace(level, offX, y + 1, offZ, dir)) {
                    double norm = VectorUtil.norm(flowX, flowY, flowZ);
                    flowX *= norm;
                    flowY *= norm;
                    flowZ *= norm;
                    flowY -= 6;
                    break;
                }
            }
        }
        double norm = VectorUtil.norm(flowX, flowY, flowZ);
        return flow.set(flowX * norm, flowY * norm, flowZ * norm);
    }

    @Override
    @Overwrite
    public Vec3 getFlow(BlockGetter level, BlockPos pos, FluidState fluidState) {
        Evolution.deprecatedMethod();
        return this.getFlow(level, pos.getX(), pos.getY(), pos.getZ(), fluidState, new Vec3d());
    }

    @Shadow
    public abstract FluidState getFlowing(int i, boolean bl);

    @Shadow
    public abstract Fluid getFlowing();

    @Override
    @Overwrite
    public float getHeight(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getHeight_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public float getHeight_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        return hasSameAbove_(fluidState, level, x, y, z) ? 1.0F : fluidState.getOwnHeight();
    }

    @Overwrite
    @DeleteMethod
    public FluidState getNewLiquid(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Unique
    private FluidState getNewLiquid(LevelReader level, int x, int y, int z, BlockState state) {
        int amount = 0;
        int sources = 0;
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            int offX = x + dir.getStepX();
            int offZ = z + dir.getStepZ();
            FluidState fluidAtSide = level.getFluidState_(offX, y, offZ);
            if (fluidAtSide.getType().isSame(this) && canPassThroughWall(dir, level, x, y, z, state, offX, y, offZ, level.getBlockState_(offX, y, offZ))) {
                if (fluidAtSide.isSource()) {
                    ++sources;
                }
                amount = Math.max(amount, fluidAtSide.getAmount());
            }
        }
        if (this.canConvertToSource() && sources >= 2) {
            if (level.getBlockState_(x, y - 1, z).getMaterial().isSolid() || this.isSourceBlockOfThisType(level.getFluidState_(x, y - 1, z))) {
                return this.getSource(false);
            }
        }
        FluidState fluidAbove = level.getFluidState_(x, y + 1, z);
        if (!fluidAbove.isEmpty() && fluidAbove.getType().isSame(this) && canPassThroughWall(Direction.UP, level, x, y, z, state, x, y + 1, z, level.getBlockState_(x, y + 1, z))) {
            return this.getFlowing(8, true);
        }
        int actualAmount = amount - this.getDropOff(level);
        if (actualAmount <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getFlowing(actualAmount, false);
    }

    @Override
    @Overwrite
    public VoxelShape getShape(FluidState fluidState, BlockGetter level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.getShape_(fluidState, level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public VoxelShape getShape_(FluidState fluidState, BlockGetter level, int x, int y, int z) {
        if (fluidState.getAmount() == 9 && hasSameAbove_(fluidState, level, x, y, z)) {
            return Shapes.block();
        }
        VoxelShape shape = this.shapes.get(fluidState);
        if (shape == null) {
            shape = Shapes.box(0, 0, 0, 1, fluidState.getHeight_(level, x, y, z), 1);
            this.shapes.put(fluidState, shape);
        }
        return shape;
    }

    @Overwrite
    @DeleteMethod
    public int getSlopeDistance(LevelReader levelReader, BlockPos blockPos, int i, Direction direction, BlockState blockState, BlockPos blockPos2, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        throw new AbstractMethodError();
    }

    protected int getSlopeDistance(LevelReader level, int x, int y, int z, int i, Direction direction, BlockState state, int originX, int originZ, Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap, Short2BooleanMap short2BooleanMap) {
        int j = 1_000;
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            if (dir != direction) {
                int offX = x + dir.getStepX();
                int offZ = z + dir.getStepZ();
                short s = getCacheKey(originX, originZ, offX, offZ);
                Pair<BlockState, FluidState> pair = short2ObjectMap.get(s);
                if (pair == null) {
                    pair = Pair.of(level.getBlockState_(offX, y, offZ), level.getFluidState_(offX, y, offZ));
                    short2ObjectMap.put(s, pair);
                }
                BlockState stateAtOff = pair.getFirst();
                FluidState fluidAtOff = pair.getSecond();
                if (this.canPassThrough(level, this.getFlowing(), x, y, z, state, dir, offX, y, offZ, stateAtOff, fluidAtOff)) {
                    boolean bl = short2BooleanMap.get(s);
                    if (!bl && !short2BooleanMap.containsKey(s)) {
                        bl = this.isWaterHole(level, this.getFlowing(), offX, y, offZ, stateAtOff, offX, y - 1, offZ, level.getBlockState_(offX, y - 1, offZ));
                        short2BooleanMap.put(s, bl);
                    }
                    if (bl) {
                        return i;
                    }
                    if (i < this.getSlopeFindDistance(level)) {
                        int k = this.getSlopeDistance(level, offX, y, offZ, i + 1, dir.getOpposite(), stateAtOff, originX, originZ, short2ObjectMap, short2BooleanMap);
                        if (k < j) {
                            j = k;
                        }
                    }
                }
            }
        }
        return j;
    }

    @Shadow
    protected abstract int getSlopeFindDistance(LevelReader levelReader);

    @Shadow
    public abstract FluidState getSource(boolean bl);

    @Overwrite
    @DeleteMethod
    public Map<Direction, FluidState> getSpread(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Unique
    private Map<Direction, FluidState> getSpread(LevelReader level, int x, int y, int z, BlockState state) {
        int i = 1_000;
        Map<Direction, FluidState> map = Maps.newEnumMap(Direction.class);
        Short2ObjectMap<Pair<BlockState, FluidState>> short2ObjectMap = new Short2ObjectOpenHashMap();
        Short2BooleanMap short2BooleanMap = new Short2BooleanOpenHashMap();
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            int offX = x + dir.getStepX();
            int offZ = z + dir.getStepZ();
            short s = getCacheKey(x, z, offX, offZ);
            Pair<BlockState, FluidState> pair = short2ObjectMap.get(s);
            if (pair == null) {
                pair = Pair.of(level.getBlockState_(offX, y, offZ), level.getFluidState_(offX, y, offZ));
                short2ObjectMap.put(s, pair);
            }
            BlockState stateAtOff = pair.getFirst();
            FluidState fluidAtOff = pair.getSecond();
            FluidState newLiquid = this.getNewLiquid(level, offX, y, offZ, stateAtOff);
            if (this.canPassThrough(level, newLiquid.getType(), x, y, z, state, dir, offX, y, offZ, stateAtOff, fluidAtOff)) {
                boolean bl = short2BooleanMap.get(s);
                if (!bl && !short2BooleanMap.containsKey(s)) {
                    bl = this.isWaterHole(level, this.getFlowing(), offX, y, offZ, stateAtOff, offX, y - 1, offZ, level.getBlockState_(offX, y - 1, offZ));
                    short2BooleanMap.put(s, bl);
                }
                int j;
                if (bl) {
                    j = 0;
                }
                else {
                    j = this.getSlopeDistance(level, offX, y, offZ, 1, dir.getOpposite(), stateAtOff, x, z, short2ObjectMap, short2BooleanMap);
                }
                if (j < i) {
                    map.clear();
                }
                if (j <= i) {
                    map.put(dir, newLiquid);
                    i = j;
                }
            }
        }
        return map;
    }

    @Overwrite
    public int getSpreadDelay(Level level, BlockPos pos, FluidState fluidState, FluidState fluidState2) {
        Evolution.deprecatedMethod();
        return this.getSpreadDelay(level, pos.getX(), pos.getY(), pos.getZ(), fluidState, fluidState2);
    }

    @Override
    public int getSpreadDelay(Level level, int x, int y, int z, FluidState state, FluidState newState) {
        return this.getTickDelay(level);
    }

    @Overwrite
    @DeleteMethod
    public boolean isSolidFace(BlockGetter level, BlockPos pos, Direction dir) {
        throw new AbstractMethodError();
    }

    @Unique
    private boolean isSolidFace(BlockGetter level, int x, int y, int z, Direction dir) {
        if (level.getFluidState_(x, y, z).getType().isSame(this)) {
            return false;
        }
        if (dir == Direction.UP) {
            return true;
        }
        BlockState blockState = level.getBlockState_(x, y, z);
        return blockState.getMaterial() != Material.ICE && blockState.isFaceSturdy_(level, x, y, z, dir);
    }

    @Shadow
    protected abstract boolean isSourceBlockOfThisType(FluidState fluidState);

    @Overwrite
    @DeleteMethod
    private boolean isWaterHole(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        throw new AbstractMethodError();
    }

    @Unique
    private boolean isWaterHole(BlockGetter level, Fluid fluid, int x, int y, int z, BlockState state, int offX, int offY, int offZ, BlockState stateAtOff) {
        if (!canPassThroughWall(Direction.DOWN, level, x, y, z, state, offX, offY, offZ, stateAtOff)) {
            return false;
        }
        return level.getFluidState_(offX, offY, offZ).getType().isSame(this) || canHoldFluid(level, offX, offY, offZ, stateAtOff, fluid);
    }

    @Overwrite
    @DeleteMethod
    private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
        throw new AbstractMethodError();
    }

    @Unique
    private int sourceNeighborCount(BlockGetter level, int x, int y, int z) {
        int source = 0;
        for (Direction dir : DirectionUtil.HORIZ_NESW) {
            if (this.isSourceBlockOfThisType(level.getFluidStateAtSide(x, y, z, dir))) {
                ++source;
            }
        }
        return source;
    }

    @Overwrite
    @DeleteMethod
    public void spread(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState) {
        throw new AbstractMethodError();
    }

    @Unique
    private void spread(LevelAccessor level, int x, int y, int z, FluidState fluid) {
        if (!fluid.isEmpty()) {
            BlockState state = level.getBlockState_(x, y, z);
            BlockState stateBelow = level.getBlockState_(x, y - 1, z);
            FluidState fluidForBelow = this.getNewLiquid(level, x, y - 1, z, stateBelow);
            if (canSpreadTo(level, x, y, z, state, Direction.DOWN, x, y - 1, z, stateBelow, level.getFluidState_(x, y - 1, z), fluidForBelow.getType())) {
                this.spreadTo_(level, x, y - 1, z, stateBelow, Direction.DOWN, fluidForBelow);
                if (this.sourceNeighborCount(level, x, y, z) >= 3) {
                    this.spreadToSides(level, x, y, z, fluid, state);
                }
            }
            else if (fluid.isSource() || !this.isWaterHole(level, fluidForBelow.getType(), x, y, z, state, x, y - 1, z, stateBelow)) {
                this.spreadToSides(level, x, y, z, fluid, state);
            }
        }
    }

    @Overwrite
    public void spreadTo(LevelAccessor level, BlockPos pos, BlockState state, Direction direction, FluidState fluid) {
        Evolution.deprecatedMethod();
        this.spreadTo_(level, pos.getX(), pos.getY(), pos.getZ(), state, direction, fluid);
    }

    @Overwrite
    @DeleteMethod
    private void spreadToSides(LevelAccessor levelAccessor, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
        throw new AbstractMethodError();
    }

    @Unique
    private void spreadToSides(LevelAccessor level, int x, int y, int z, FluidState fluid, BlockState state) {
        int amount = fluid.getAmount() - this.getDropOff(level);
        if (fluid.getValue(FALLING)) {
            amount = 7;
        }
        if (amount > 0) {
            Map<Direction, FluidState> map = this.getSpread(level, x, y, z, state);
            for (Map.Entry<Direction, FluidState> e : map.entrySet()) {
                Direction dir = e.getKey();
                FluidState fluidState = e.getValue();
                int offX = x + dir.getStepX();
                int offZ = z + dir.getStepZ();
                BlockState stateAtOff = level.getBlockState_(offX, y, offZ);
                if (canSpreadTo(level, x, y, z, state, dir, offX, y, offZ, stateAtOff, level.getFluidState_(offX, y, offZ), fluidState.getType())) {
                    this.spreadTo_(level, offX, y, offZ, stateAtOff, dir, fluidState);
                }
            }
        }
    }

    @Override
    public void spreadTo_(LevelAccessor level, int x, int y, int z, BlockState state, Direction direction, FluidState fluid) {
        if (state.getBlock() instanceof LiquidBlockContainer container) {
            container.placeLiquid(level, new BlockPos(x, y, z), state, fluid);
        }
        else {
            if (!state.isAir()) {
                this.beforeDestroyingBlock_(level, x, y, z, state);
            }
            level.setBlockAndUpdate_(x, y, z, fluid.createLegacyBlock());
        }
    }

    @Override
    @Overwrite
    @DeleteMethod
    public void tick(Level level, BlockPos blockPos, FluidState fluidState) {
        throw new AbstractMethodError();
    }

    @Override
    public void tick_(Level level, int x, int y, int z, FluidState state) {
        if (!state.isSource()) {
            FluidState newLiquid = this.getNewLiquid(level, x, y, z, level.getBlockState_(x, y, z));
            int tickrate = this.getSpreadDelay(level, x, y, z, state, newLiquid);
            if (newLiquid.isEmpty()) {
                state = newLiquid;
                level.setBlockAndUpdate_(x, y, z, Blocks.AIR.defaultBlockState());
            }
            else if (newLiquid != state) {
                state = newLiquid;
                BlockState legacyBlock = newLiquid.createLegacyBlock();
                level.setBlock_(x, y, z, legacyBlock, BlockFlags.BLOCK_UPDATE);
                level.scheduleTick(new BlockPos(x, y, z), newLiquid.getType(), tickrate);
                level.updateNeighborsAt_(x, y, z, legacyBlock.getBlock());
            }
        }
        this.spread(level, x, y, z, state);
    }
}
