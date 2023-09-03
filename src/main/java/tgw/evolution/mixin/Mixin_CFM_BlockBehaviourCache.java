package tgw.evolution.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockStateCache;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.math.DirectionUtil;

@Mixin(BlockBehaviour.BlockStateBase.Cache.class)
public abstract class Mixin_CFM_BlockBehaviourCache implements PatchBlockStateCache {

    /**
     * Bit 0: solidRender; <br>
     * Bit 1: propagatesSkylightDown; <br>
     * Bit 2: largeCollisionShape; <br>
     * Bit 3: isCollisionShapeFullBlock; <br>
     * Bit 4 ~ 21: isFaceSturdy; <br>
     */
    private final int flags;
    @Mutable @Shadow @Final @RestoreFinal public VoxelShape collisionShape;
    @Mutable @Shadow @Final @RestoreFinal public int lightBlock;
    @Mutable @Shadow @Final @RestoreFinal public VoxelShape @Nullable [] occlusionShapes;
    @Shadow @Final @DeleteField protected boolean isCollisionShapeFullBlock;
    @Shadow @Final @DeleteField protected boolean largeCollisionShape;
    @Shadow @Final @DeleteField protected boolean solidRender;
    @Shadow @Final @DeleteField boolean propagatesSkylightDown;
    @Shadow @Final @DeleteField private boolean[] faceSturdy;

    @ModifyConstructor
    Mixin_CFM_BlockBehaviourCache(BlockState state) {
        Block block = state.getBlock();
        int f = state.isSolidRender_(EmptyBlockGetter.INSTANCE, 0, 0, 0) ? 1 : 0;
        if (block.propagatesSkylightDown_(state, EmptyBlockGetter.INSTANCE, 0, 0, 0)) {
            f |= 2;
        }
        this.lightBlock = block.getLightBlock_(state, EmptyBlockGetter.INSTANCE, 0, 0, 0);
        if (!state.canOcclude()) {
            this.occlusionShapes = null;
        }
        else {
            this.occlusionShapes = new VoxelShape[DirectionUtil.ALL.length];
            VoxelShape shape = block.getOcclusionShape_(state, EmptyBlockGetter.INSTANCE, 0, 0, 0);
            for (Direction direction : DirectionUtil.ALL) {
                this.occlusionShapes[direction.ordinal()] = Shapes.getFaceShape(shape, direction);
            }
        }
        this.collisionShape = block.getCollisionShape_(state, EmptyBlockGetter.INSTANCE, 0, 0, 0, null);
        if (!this.collisionShape.isEmpty() && block.getOffsetType() != BlockBehaviour.OffsetType.NONE) {
            throw new IllegalStateException(
                    String.format("%s has a collision shape and an offset type, but is not marked as dynamicShape in its properties.",
                                  Registry.BLOCK.getKey(block)));
        }
        for (Direction.Axis axis : DirectionUtil.AXIS) {
            if (this.collisionShape.min(axis) < 0 || this.collisionShape.max(axis) > 1) {
                f |= 4;
                break;
            }
        }
        if (Block.isShapeFullBlock(state.getCollisionShape_(EmptyBlockGetter.INSTANCE, 0, 0, 0))) {
            f |= 8;
        }
        for (Direction direction : DirectionUtil.ALL) {
            for (SupportType supportType : ArrayHelper.SUPPORT_TYPE) {
                if (supportType.isSupporting_(state, EmptyBlockGetter.INSTANCE, 0, 0, 0, direction)) {
                    f |= 1 << direction.ordinal() * 3 + supportType.ordinal() + 4;
                }
            }
        }
        this.flags = f;
    }

    @DeleteMethod
    @Overwrite
    private static int getFaceSupportIndex(Direction direction, SupportType supportType) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean isCollisionShapeFullBlock() {
        return (this.flags & 8) != 0;
    }

    @Overwrite
    public boolean isFaceSturdy(Direction direction, SupportType supportType) {
        return (this.flags & 1 << direction.ordinal() * 3 + supportType.ordinal() + 4) != 0;
    }

    @Override
    public boolean largeCollisionShape() {
        return (this.flags & 4) != 0;
    }

    @Override
    public boolean propagatesSkylightDown() {
        return (this.flags & 2) != 0;
    }

    @Override
    public boolean solidRender() {
        return (this.flags & 1) != 0;
    }
}
