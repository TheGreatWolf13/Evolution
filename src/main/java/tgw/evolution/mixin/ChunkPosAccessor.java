package tgw.evolution.mixin;

import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkPos.class)
public interface ChunkPosAccessor {

    @Accessor(value = "x")
    void setX(int x);

    @Accessor(value = "z")
    void setZ(int z);
}
