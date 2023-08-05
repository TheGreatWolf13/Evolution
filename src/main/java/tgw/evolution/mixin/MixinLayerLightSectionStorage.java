package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

@Mixin(LayerLightSectionStorage.class)
public abstract class MixinLayerLightSectionStorage<M extends DataLayerStorageMap<M>> extends SectionTracker {

    @Shadow @Final protected LongSet changedSections;
    @Shadow protected volatile boolean hasToRemove;
    @Mutable @Shadow @Final protected Long2ObjectMap<DataLayer> queuedSections;
    @Shadow @Final protected M updatingSectionData;
    @Shadow @Final private LongSet columnsToRetainQueuedDataFor;
    @Mutable @Shadow @Final private LongSet toRemove;
    @Mutable @Shadow @Final private LongSet untrustedSections;

    public MixinLayerLightSectionStorage(int i, int j, int k) {
        super(i, j, k);
    }

    @Shadow
    protected abstract void checkEdgesForSection(LayerLightEngine<M, ?> layerLightEngine, long l);

    @Shadow
    protected abstract void clearQueuedSectionBlocks(LayerLightEngine<?, ?> layerLightEngine, long l);

    @Shadow
    protected abstract boolean hasInconsistencies();

    @Overwrite
    public void markNewInconsistencies(LayerLightEngine<M, ?> lightEngine, boolean bl, boolean bl2) {
        if (this.hasInconsistencies() || !this.queuedSections.isEmpty()) {
            LSet toRemove = (LSet) this.toRemove;
            for (LSet.Entry e = toRemove.fastEntries(); e != null; e = toRemove.fastEntries()) {
                long l = e.get();
                this.clearQueuedSectionBlocks(lightEngine, l);
                if (this.columnsToRetainQueuedDataFor.contains(SectionPos.getZeroNode(l))) {
                    DataLayer queued = this.queuedSections.remove(l);
                    if (queued != null) {
                        this.queuedSections.put(l, queued);
                    }
                    else {
                        DataLayer updating = this.updatingSectionData.removeLayer(l);
                        if (updating != null) {
                            this.queuedSections.put(l, updating);
                        }
                    }
                }
            }
            this.updatingSectionData.clearCache();
            for (LSet.Entry e = toRemove.fastEntries(); e != null; e = toRemove.fastEntries()) {
                this.onNodeRemoved(e.get());
            }
            toRemove.clear();
            this.hasToRemove = false;
            L2OMap<DataLayer> queuedSections = (L2OMap<DataLayer>) this.queuedSections;
            for (L2OMap.Entry<DataLayer> e = queuedSections.fastEntries(); e != null; e = queuedSections.fastEntries()) {
                long l = e.key();
                if (this.storingLightForSection(l)) {
                    DataLayer dataLayer = e.value();
                    if (this.updatingSectionData.getLayer(l) != dataLayer) {
                        this.clearQueuedSectionBlocks(lightEngine, l);
                        this.updatingSectionData.setLayer(l, dataLayer);
                        this.changedSections.add(l);
                    }
                }
            }
            this.updatingSectionData.clearCache();
            if (!bl2) {
                for (L2OMap.Entry<DataLayer> e = queuedSections.fastEntries(); e != null; e = queuedSections.fastEntries()) {
                    this.checkEdgesForSection(lightEngine, e.key());
                }
            }
            else {
                LSet untrustedSections = (LSet) this.untrustedSections;
                for (LSet.Entry e = untrustedSections.fastEntries(); e != null; e = untrustedSections.fastEntries()) {
                    this.checkEdgesForSection(lightEngine, e.get());
                }
            }
            this.untrustedSections.clear();
            for (L2OMap.Entry<DataLayer> e = queuedSections.fastEntries(); e != null; e = queuedSections.fastEntries()) {
                long l = e.key();
                if (this.storingLightForSection(l)) {
                    queuedSections.remove(l);
                }
            }
        }
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;" +
                                                                    "toRemove:Lit/unimi/dsi/fastutil/longs/LongSet;", opcode = Opcodes.PUTFIELD))
    private void onInit0(LayerLightSectionStorage instance, LongSet value) {
        this.toRemove = new LHashSet();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;" +
                                                                    "untrustedSections:Lit/unimi/dsi/fastutil/longs/LongSet;", opcode =
            Opcodes.PUTFIELD))
    private void onInit1(LayerLightSectionStorage instance, LongSet value) {
        this.untrustedSections = new LHashSet();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;" +
                                                                    "queuedSections:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode =
            Opcodes.PUTFIELD))
    private void onInit2(LayerLightSectionStorage instance, Long2ObjectMap<DataLayer> value) {
        this.queuedSections = L2OMap.synchronize(new L2OHashMap<>());
    }

    @Shadow
    protected abstract void onNodeRemoved(long l);

    @Shadow
    protected abstract boolean storingLightForSection(long l);
}
