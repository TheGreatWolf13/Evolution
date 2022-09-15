package tgw.evolution.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IChunkPosPatch;

@Mixin(ChunkPos.class)
public abstract class ChunkPosMixin implements IChunkPosPatch {
    @Mutable
    @Shadow
    @Final
    public int x;

    @Mutable
    @Shadow
    @Final
    public int z;

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setZ(int z) {
        this.z = z;
    }
}
