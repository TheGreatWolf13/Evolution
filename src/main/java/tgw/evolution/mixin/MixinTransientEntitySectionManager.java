package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.entity.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchTransientEntitySectionManager;
import tgw.evolution.util.collection.lists.OList;

@Mixin(TransientEntitySectionManager.class)
public abstract class MixinTransientEntitySectionManager<T extends EntityAccess> implements PatchTransientEntitySectionManager {

    @Shadow @Final LevelCallback<T> callbacks;
    @Shadow @Final EntitySectionStorage<T> sectionStorage;
    @Shadow @Final private LongSet tickingChunks;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void startTicking(ChunkPos pos) {
        Evolution.deprecatedMethod();
        this.startTicking(pos.x, pos.z);
    }

    @Override
    public void startTicking(int chunkX, int chunkZ) {
        long pos = ChunkPos.asLong(chunkX, chunkZ);
        this.tickingChunks.add(pos);
        this.sectionStorage.getExistingSectionsInChunk_(pos, s -> {
            EntitySection<T> section = (EntitySection<T>) s;
            Visibility visibility = section.updateChunkStatus(Visibility.TICKING);
            if (!visibility.isTicking()) {
                OList<T> entities = section.getEntities_();
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.callbacks.onTickingStart(t);
                    }
                }
            }
        });
    }
}
