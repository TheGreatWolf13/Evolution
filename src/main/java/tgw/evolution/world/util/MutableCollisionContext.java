package tgw.evolution.world.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class MutableCollisionContext implements CollisionContext {

    private @Nullable Entity entity;
    private double entityBottom;
    /**
     * Bit 0: isDescending;<br>
     * Bit 1: instanceof LivingEntity;<br>
     */
    private byte flags;
    private ItemStack heldItem = ItemStack.EMPTY;

    @Override
    public boolean canStandOnFluid(FluidState first, FluidState second) {
        if ((this.flags & 2) == 0) {
            return false;
        }
        assert this.entity != null;
        return ((LivingEntity) this.entity).canStandOnFluid(second) && !first.getType().isSame(second.getType());
    }

    @Override
    public boolean isAbove(VoxelShape shape, BlockPos pos, boolean canAscend) {
        if (this.entity == null) {
            return canAscend;
        }
        return this.entityBottom > pos.getY() + shape.max(Direction.Axis.Y) - 1e-5;
    }

    @Override
    public boolean isDescending() {
        return (this.flags & 1) != 0;
    }

    @Override
    public boolean isHoldingItem(Item item) {
        return this.heldItem.is(item);
    }

    public void reset() {
        this.entity = null;
        this.heldItem = ItemStack.EMPTY;
    }

    public CollisionContext set(@Nullable Entity entity) {
        if (entity == null) {
            this.flags = 0;
            this.entityBottom = -Double.MAX_VALUE;
        }
        else {
            this.entity = entity;
            this.flags = (byte) (entity.isDescending() ? 1 : 0);
            this.entityBottom = entity.getY();
            if (entity instanceof LivingEntity living) {
                this.flags |= 2;
                this.heldItem = living.getMainHandItem();
            }
        }
        return this;
    }
}
