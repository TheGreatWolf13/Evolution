package tgw.evolution.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public class MutableBlockInWorld extends BlockInWorld {

    public MutableBlockInWorld() {
        //noinspection ConstantConditions
        super(null, BlockPos.ZERO, true);
        this.pos = new BlockPos.MutableBlockPos();
    }

    @Override
    public @Nullable BlockEntity getEntity() {
        if (this.entity == null && !this.cachedEntity) {
            this.entity = this.level.getBlockEntity_(this.pos);
            this.cachedEntity = true;
        }
        return this.entity;
    }

    @Override
    public BlockState getState() {
        if (this.state == null && (this.loadChunks || this.level.hasChunkAt(this.pos.getX(), this.pos.getZ()))) {
            this.state = this.level.getBlockState_(this.pos);
        }
        return this.state;
    }

    public MutableBlockInWorld set(LevelReader level, int x, int y, int z, boolean loadChunks) {
        this.level = level;
        ((BlockPos.MutableBlockPos) this.pos).set(x, y, z);
        this.loadChunks = loadChunks;
        this.cachedEntity = false;
        this.state = null;
        this.entity = null;
        return this;
    }
}
