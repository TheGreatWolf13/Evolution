package tgw.evolution.mixin;

import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchChunkSource;

@Mixin(ChunkSource.class)
public abstract class MixinChunkSource implements LightChunkGetter, AutoCloseable, PatchChunkSource {
}
