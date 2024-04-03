package tgw.evolution.blocks;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionShapes;
import tgw.evolution.util.constants.HarvestLevel;
import tgw.evolution.util.constants.WoodVariant;
import tgw.evolution.util.math.DirectionUtil;
import tgw.evolution.util.math.MathHelper;

import java.util.Random;
import java.util.function.Consumer;

import static tgw.evolution.init.EvolutionBStates.*;

public class BlockPlanks extends BlockPhysics {

    protected static final VoxelShape[] SHAPE_CACHE = new VoxelShape[64];

    static {
        SHAPE_CACHE[0] = Shapes.empty();
        SHAPE_CACHE[63] = Shapes.block();
        for (int i = 1; i < 63; ++i) {
            VoxelShape shape = Shapes.empty();
            for (Direction direction : DirectionUtil.ALL) {
                if ((i & 1 << direction.ordinal()) != 0) {
                    shape = MathHelper.union(shape, EvolutionShapes.directionToShape2Thickness(direction));
                }
            }
            SHAPE_CACHE[i] = shape;
        }
    }

    private final WoodVariant variant;

    public BlockPlanks(WoodVariant variant) {
        super(Properties.of(Material.WOOD).strength(6.0f, 2.0f).sound(SoundType.WOOD));
        this.variant = variant;
    }

    private static int getIndex(BlockState state) {
        int data = 0;
        for (Direction direction : DirectionUtil.ALL) {
            if (state.getValue(directionToProperty(direction))) {
                data |= 1 << direction.ordinal();
            }
        }
        return data;
    }

    @Override
    public boolean canBeReplaced_(BlockState state, Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isSecondaryUseActive()) {
            return false;
        }
        if (player.getItemInHand(hand).getItem() == this.variant.get(EvolutionItems.PLANK)) {
            double dx = hitResult.x() - x;
            double dy = hitResult.y() - y;
            double dz = hitResult.z() - z;
            if (dx > 0 && dx < 1 && dy > 0 && dy < 1 && dz > 0 && dz < 1) {
                return true;
            }
            Direction face = hitResult.getDirection().getOpposite();
            return switch (face) {
                case NORTH -> hitResult.z() - z == 0;
                case SOUTH -> hitResult.z() - z == 1;
                case EAST -> hitResult.x() - x == 1;
                case WEST -> hitResult.x() - x == 0;
                case DOWN -> hitResult.y() - y == 0;
                case UP -> hitResult.y() - y == 1;
            };
        }
        return false;
    }

    @Override
    public boolean canSurvive_(BlockState state, LevelReader level, int x, int y, int z) {
        //if 3 or more faces, if one is supported, all of them are
        //actually, this works for any number of faces if they are all connected
        //the only way for they to not be connected is 2 faces opposite of each other.
        if (state.getValue(DISTANCE_1_4) == 4) {
            return false;
        }
        int hasSet = 0;
        int count = 0;
        Direction face = null;
        boolean opposite = false;
        for (Direction dir : DirectionUtil.ALL) {
            if (state.getValue(directionToProperty(dir))) {
                hasSet |= 1 << dir.ordinal();
                ++count;
                if (face == null) {
                    face = dir;
                }
                else {
                    opposite = count == 2 && face == dir.getOpposite();
                }
            }
        }
        if (count == 0) {
            return false;
        }
        boolean stable = count != 2 || !opposite;
        for (Direction dir : DirectionUtil.ALL) {
            BlockState stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
            int supported = this.getSupportedSides(stateAtSide, dir);
            int actualSupported = hasSet & supported;
            if (actualSupported != 0 && stable) {
                return true;
            }
            hasSet &= ~actualSupported;
            if (hasSet == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dropLoot(BlockState state, ServerLevel level, int x, int y, int z, ItemStack tool, @Nullable BlockEntity tile, @Nullable Entity entity, Random random, Consumer<ItemStack> consumer) {
        if (entity != null) {
            super.dropLoot(state, level, x, y, z, tool, tile, entity, random, consumer);
        }
        else {
            int count = 0;
            for (Direction direction : DirectionUtil.ALL) {
                if (state.getValue(directionToProperty(direction))) {
                    ++count;
                }
            }
            consumer.accept(new ItemStack(this, count));
        }
    }

    @Override
    public BlockState getDestroyingState(BlockState state, Level level, int x, int y, int z, @Nullable Direction face, double hitX, double hitY, double hitZ) {
        if (face == null) {
            return state;
        }
        int count = 0;
        int hasSide = 0;
        for (Direction direction : DirectionUtil.ALL) {
            if (state.getValue(directionToProperty(direction))) {
                hasSide |= 1 << direction.ordinal();
                ++count;
            }
        }
        if (count < 2) {
            return state;
        }
        double dx = hitX - x;
        double dy = hitY - y;
        double dz = hitZ - z;
        Direction.Axis axis = face.getAxis();
        double dAxis = DirectionUtil.choose(axis, dx, dy, dz);
        if (dAxis == 1 || dAxis == 0) {
            if ((hasSide & 1 << face.ordinal()) != 0) {
                return this.defaultBlockState().setValue(directionToProperty(face), true);
            }
            Direction.Axis primaryAxis = DirectionUtil.backward(axis);
            Direction.Axis secondaryAxis = DirectionUtil.forward(axis);
            Direction chosenDir = DirectionUtil.chooseByDistance(2 / 16.0, 14 / 16.0, primaryAxis, DirectionUtil.choose(primaryAxis, dx, dy, dz), secondaryAxis, DirectionUtil.choose(secondaryAxis, dx, dy, dz), face, hasSide);
            return this.defaultBlockState().setValue(directionToProperty(chosenDir), true);
        }
        return this.defaultBlockState().setValue(directionToProperty(face.getOpposite()), true);
    }

    @Override
    public float getFrictionCoefficient(BlockState state) {
        return 0.7f;
    }

    @Override
    public int getHarvestLevel(BlockState state, Level level, int x, int y, int z) {
        return HarvestLevel.STONE;
    }

    @Override
    public VoxelShape getShape_(BlockState state, BlockGetter level, int x, int y, int z, @Nullable Entity entity) {
        return SHAPE_CACHE[getIndex(state)];
    }

    @Override
    public @Nullable BlockState getStateForPlacement_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockState state = level.getBlockState_(x, y, z);
        if (state.getBlock() == this.variant.get(EvolutionBlocks.PLANKS)) {
            int hasSide = 0;
            for (Direction dir : DirectionUtil.ALL) {
                if (state.getValue(directionToProperty(dir))) {
                    hasSide |= 1 << dir.ordinal();
                }
            }
            hasSide = ~hasSide;
            Direction face = hitResult.getDirection();
            BooleanProperty property = directionToProperty(face.getOpposite());
            double dx = hitResult.x() - x;
            double dy = hitResult.y() - y;
            double dz = hitResult.z() - z;
            Direction.Axis axis = face.getAxis();
            Direction.Axis primaryAxis = DirectionUtil.backward(axis);
            Direction.Axis secondaryAxis = DirectionUtil.forward(axis);
            Direction chosenDir = DirectionUtil.chooseByDistance(0.25, 0.75, primaryAxis, DirectionUtil.choose(primaryAxis, dx, dy, dz), secondaryAxis, DirectionUtil.choose(secondaryAxis, dx, dy, dz), state.getValue(property) ? face : face.getOpposite(), hasSide);
            return this.updateDistance(state.setValue(directionToProperty(chosenDir), true), level, x, y, z);
        }
        Direction face = hitResult.getDirection();
        Direction.Axis axis = face.getAxis();
        double dx = hitResult.x() - x;
        double dy = hitResult.y() - y;
        double dz = hitResult.z() - z;
        Direction.Axis primaryAxis = DirectionUtil.backward(axis);
        Direction.Axis secondaryAxis = DirectionUtil.forward(axis);
        Direction chosenDir = DirectionUtil.chooseByDistance(0.25, 0.75, primaryAxis, DirectionUtil.choose(primaryAxis, dx, dy, dz), secondaryAxis, DirectionUtil.choose(secondaryAxis, dx, dy, dz), face.getOpposite(), 0b11_1111);
        return this.updateDistance(this.defaultBlockState().setValue(directionToProperty(chosenDir), true), level, x, y, z);
    }

    @Override
    public BlockState playerWillDestroy_(Level level, int x, int y, int z, BlockState state, Player player, Direction face, double hitX, double hitY, double hitZ) {
        int count = 0;
        int hasSide = 0;
        for (Direction direction : DirectionUtil.ALL) {
            if (state.getValue(directionToProperty(direction))) {
                hasSide |= 1 << direction.ordinal();
                ++count;
            }
        }
        if (count < 2) {
            this.spawnDestroyParticles_(level, player, x, y, z, state);
            return level.getFluidState_(x, y, z).createLegacyBlock();
        }
        double dx = hitX - x;
        double dy = hitY - y;
        double dz = hitZ - z;
        Direction.Axis axis = face.getAxis();
        double dAxis = DirectionUtil.choose(axis, dx, dy, dz);
        if (dAxis == 1 || dAxis == 0) {
            if ((hasSide & 1 << face.ordinal()) != 0) {
                BooleanProperty property = directionToProperty(face);
                this.spawnDestroyParticles_(level, player, x, y, z, this.defaultBlockState().setValue(property, true));
                return this.updateDistance(state.setValue(property, false), level, x, y, z);
            }
            Direction.Axis primaryAxis = DirectionUtil.backward(axis);
            Direction.Axis secondaryAxis = DirectionUtil.forward(axis);
            Direction chosenDir = DirectionUtil.chooseByDistance(2 / 16.0, 14 / 16.0, primaryAxis, DirectionUtil.choose(primaryAxis, dx, dy, dz), secondaryAxis, DirectionUtil.choose(secondaryAxis, dx, dy, dz), face, hasSide);
            BooleanProperty property = directionToProperty(chosenDir);
            this.spawnDestroyParticles_(level, player, x, y, z, this.defaultBlockState().setValue(property, true));
            return this.updateDistance(state.setValue(property, false), level, x, y, z);
        }
        BooleanProperty property = directionToProperty(face.getOpposite());
        this.spawnDestroyParticles_(level, player, x, y, z, this.defaultBlockState().setValue(property, true));
        return this.updateDistance(state.setValue(property, false), level, x, y, z);
    }

    @Override
    public BlockState updateShape_(BlockState state, Direction from, BlockState fromState, LevelAccessor level, int x, int y, int z, int fromX, int fromY, int fromZ) {
        return this.updateDistance(state, level, x, y, z);
    }

    @Override
    public boolean updatesSelf(BlockState state, Level level, int x, int y, int z) {
        return state.getValue(DISTANCE_1_4) == 4;
    }

    @Override
    public boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DISTANCE_1_4);
        builder.add(UP);
        builder.add(DOWN);
        builder.add(NORTH);
        builder.add(SOUTH);
        builder.add(EAST);
        builder.add(WEST);
    }

    protected int getSupportedSides(BlockState state, Direction dir) {
        Block block = state.getBlock();
        if (block instanceof BlockLog) {
            return DirectionUtil.fullSetExcept(dir.getOpposite());
        }
        if (block instanceof BlockPlanks) {
            return this.getSupportedSidesFor(state, dir);
        }
        return 0;
    }

    protected int getSupportedSidesFor(BlockState state, Direction dir) {
        int distance = state.getValue(DISTANCE_1_4);
        if (distance >= 3) {
            return 0;
        }
        if (state.getValue(directionToProperty(dir.getOpposite()))) {
            return distance << 6 | DirectionUtil.fullSetExcept(dir.getOpposite());
        }
        int data = 0;
        for (Direction direction : DirectionUtil.ALL) {
            if (direction == dir) {
                continue;
            }
            if (state.getValue(directionToProperty(direction))) {
                data |= 1 << direction.ordinal();
            }
        }
        return distance << 6 | data;
    }

    protected BlockState updateDistance(BlockState state, BlockAndTintGetter level, int x, int y, int z) {
        int hasSet = 0;
        int count = 0;
        Direction face = null;
        for (Direction dir : DirectionUtil.ALL) {
            if (state.getValue(directionToProperty(dir))) {
                hasSet |= 1 << dir.ordinal();
                ++count;
                if (face == null) {
                    face = dir;
                }
            }
        }
        if (count == 0) {
            return Blocks.AIR.defaultBlockState();
        }
        int minDistance = 4;
        for (Direction dir : DirectionUtil.ALL) {
            BlockState stateAtSide = level.getBlockStateAtSide(x, y, z, dir);
            int supported = this.getSupportedSides(stateAtSide, dir);
            int actualSupported = hasSet & supported;
            if (actualSupported != 0) {
                int distance = (supported >> 6) + 1;
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        return state.setValue(DISTANCE_1_4, minDistance);
    }
}
