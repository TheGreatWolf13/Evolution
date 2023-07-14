package tgw.evolution.patches;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.client.renderer.chunk.EvModelDataManager;
import tgw.evolution.util.math.AABBMutable;

public interface PatchBlockEntity {

    AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                                         Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    /**
     * DO NOT CALL EXTERNALLY.<br>
     * DO NOT OVERRIDE.<br>
     * Is implemented on {@link BlockEntity} via mixins. Use for returning a custom {@link AABB} on
     * {@link PatchBlockEntity#getRenderBoundingBox()}.
     */
    default AABBMutable _getBBForRendering() {
        throw new AbstractMethodError();
    }

    /**
     * DO NOT CALL EXTERNALLY.<br>
     * DO NOT OVERRIDE. <br>
     * Is implemented on {@link BlockEntity} via mixins.
     */
    default BlockEntity _self() {
        throw new AbstractMethodError();
    }

    /**
     * Allows you to return additional model data.
     * This data can be used to provide additional functionality in your {@link BakedModel}
     * You need to schedule a refresh of you model data via {@link #requestModelDataUpdate()} if the result of this function changes.
     * <b>Note that this method may be called on a chunk render thread instead of the main client thread</b>
     *
     * @return Your model data
     */
    default IModelData getModelData() {
        return IModelData.EMPTY;
    }

    @Contract(pure = true)
    default AABB getRenderBoundingBox() {
        BlockPos pos = this._self().getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return this._getBBForRendering().setUnchecked(x, y, z, x + 1, y + 1, z + 1);
    }

    /**
     * DO NOT OVERRIDE. <br>
     * Requests a refresh for the model data of your TE
     * Call this every time your {@link #getModelData()} changes
     */
    default void requestModelDataUpdate() {
        BlockEntity te = this._self();
        Level level = te.getLevel();
        if (level != null && level.isClientSide) {
            EvModelDataManager.requestModelDataRefresh(te);
        }
    }
}
