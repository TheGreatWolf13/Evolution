package tgw.evolution.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import tgw.evolution.util.math.AABBMutable;

public abstract class ContainerChecker<T extends IContainerCheckable> {

    private final AABBMutable aabb = new AABBMutable();
    private int openCount;

    public void decrementOpeners(Player player, Level level, BlockPos pos, T obj) {
        int i = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(level, pos, obj);
        }
        this.openerCountChanged(level, pos, obj, i, this.openCount);
    }

    private int getOpenCount(EntityGetter level, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        this.aabb.set(x - 5.0F, y - 5.0F, z - 5.0F, (x + 1) + 5.0F, (y + 1) + 5.0F, (z + 1) + 5.0F);
        return level.getEntities(EntityTypeTest.forClass(Player.class), this.aabb, this::isOwnContainer).size();
    }

    public int getOpenerCount() {
        return this.openCount;
    }

    public void incrementOpeners(Player player, Level level, BlockPos pos, T obj) {
        int i = this.openCount++;
        if (i == 0) {
            this.onOpen(level, pos, obj);
            this.scheduleRecheck(level, pos, obj);
        }
        this.openerCountChanged(level, pos, obj, i, this.openCount);
    }

    protected abstract boolean isOwnContainer(Player player);

    protected abstract void onClose(Level level, BlockPos pos, T obj);

    protected abstract void onOpen(Level level, BlockPos pos, T obj);

    protected abstract void openerCountChanged(Level level, BlockPos pos, T obj, int count, int newCount);

    public void recheckOpeners(Level level, BlockPos pos, T obj) {
        int openCount = this.getOpenCount(level, pos);
        int currentOpenCount = this.openCount;
        if (currentOpenCount != openCount) {
            boolean shouldBeOpen = openCount != 0;
            boolean isOpen = currentOpenCount != 0;
            if (shouldBeOpen && !isOpen) {
                this.onOpen(level, pos, obj);
            }
            else if (!shouldBeOpen) {
                this.onClose(level, pos, obj);
            }
            this.openCount = openCount;
        }
        this.openerCountChanged(level, pos, obj, currentOpenCount, openCount);
        if (openCount > 0) {
            this.scheduleRecheck(level, pos, obj);
        }
    }

    public abstract void scheduleRecheck(Level level, BlockPos pos, T state);
}
