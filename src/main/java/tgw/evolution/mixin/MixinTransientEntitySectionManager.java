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
        this.startTicking_(pos.toLong());
    }

    @Override
    public void startTicking_(long chunkPos) {
        this.tickingChunks.add(chunkPos);
        this.sectionStorage.getExistingSectionsInChunk_(chunkPos, section -> {
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

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void stopTicking(ChunkPos pos) {
        Evolution.deprecatedMethod();
        this.stopTicking_(pos.toLong());
    }

    @Override
    public void stopTicking_(long chunkPos) {
        this.tickingChunks.remove(chunkPos);
        this.sectionStorage.getExistingSectionsInChunk_(chunkPos, entitySection -> {
            Visibility visibility = entitySection.updateChunkStatus(Visibility.TRACKED);
            if (visibility.isTicking()) {
                OList<T> entities = entitySection.getEntities_();
                for (int i = 0, len = entities.size(); i < len; ++i) {
                    T t = entities.get(i);
                    if (!t.isAlwaysTicking()) {
                        this.callbacks.onTickingEnd(t);
                    }
                }
            }
        });
    }
}
