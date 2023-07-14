package tgw.evolution.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkPos.class)
public interface AccessorChunkPos {

    @Mutable
    @Accessor(value = "x")
    void setX(int x);

    @Mutable
    @Accessor(value = "z")
    void setZ(int z);
}
