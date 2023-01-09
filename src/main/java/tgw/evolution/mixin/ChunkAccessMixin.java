package tgw.evolution.mixin;

import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ChunkAccess.class)
public abstract class ChunkAccessMixin {

    @Shadow
    @Final
    protected Map<Heightmap.Types, Heightmap> heightmaps;

    /**
     * @author TheGreatWolf
     * @reason Remove computeIfAbsent
     */
    @Overwrite
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types type) {
        Heightmap heightmap = this.heightmaps.get(type);
        if (heightmap == null) {
            heightmap = new Heightmap((ChunkAccess) (Object) this, type);
            this.heightmaps.put(type, heightmap);
        }
        return heightmap;
    }
}
