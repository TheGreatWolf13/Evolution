package tgw.evolution.patches;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Contract;
import tgw.evolution.util.math.AABBMutable;

public interface PatchBlockEntity {

    AABB INFINITE_EXTENT_AABB = new AABB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

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

    @Contract(pure = true)
    default AABB getRenderBoundingBox() {
        BlockPos pos = this._self().getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return this._getBBForRendering().setUnchecked(x, y, z, x + 1, y + 1, z + 1);
    }
}
