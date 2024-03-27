package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightEngine;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.util.math.DirectionUtil;

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

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public DataLayer createDataLayer(long pos) {
        DataLayer dataLayer;
        synchronized (this.queuedSections) {
            dataLayer = this.queuedSections.get(pos);
        }
        return dataLayer != null ? dataLayer : new DataLayer();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public @Nullable DataLayer getDataLayerData(long pos) {
        DataLayer dataLayer;
        synchronized (this.queuedSections) {
            dataLayer = this.queuedSections.get(pos);
        }
        return dataLayer != null ? dataLayer : this.getDataLayer(pos, false);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void markNewInconsistencies(LayerLightEngine<M, ?> lightEngine, boolean bl, boolean bl2) {
        synchronized (this.queuedSections) {
            if (this.hasInconsistencies() || !this.queuedSections.isEmpty()) {
                LSet toRemove = (LSet) this.toRemove;
                for (long it = toRemove.beginIteration(); toRemove.hasNextIteration(it); it = toRemove.nextEntry(it)) {
                    long l = toRemove.getIteration(it);
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
                for (long it = toRemove.beginIteration(); toRemove.hasNextIteration(it); it = toRemove.nextEntry(it)) {
                    this.onNodeRemoved(toRemove.getIteration(it));
                }
                toRemove.clear();
                this.hasToRemove = false;
                L2OMap<DataLayer> queuedSections = (L2OMap<DataLayer>) this.queuedSections;
                for (long it = queuedSections.beginIteration(); queuedSections.hasNextIteration(it); it = queuedSections.nextEntry(it)) {
                    long l = queuedSections.getIterationKey(it);
                    if (this.storingLightForSection(l)) {
                        DataLayer dataLayer = queuedSections.getIterationValue(it);
                        if (this.updatingSectionData.getLayer(l) != dataLayer) {
                            this.clearQueuedSectionBlocks(lightEngine, l);
                            this.updatingSectionData.setLayer(l, dataLayer);
                            this.changedSections.add(l);
                        }
                    }
                }
                this.updatingSectionData.clearCache();
                if (!bl2) {
                    for (long it = queuedSections.beginIteration(); queuedSections.hasNextIteration(it); it = queuedSections.nextEntry(it)) {
                        this.checkEdgesForSection(lightEngine, queuedSections.getIterationKey(it));
                    }
                }
                else {
                    LSet untrustedSections = (LSet) this.untrustedSections;
                    for (long it = untrustedSections.beginIteration(); untrustedSections.hasNextIteration(it); it = untrustedSections.nextEntry(it)) {
                        this.checkEdgesForSection(lightEngine, untrustedSections.getIteration(it));
                    }
                }
                this.untrustedSections.clear();
                for (long it = queuedSections.beginIteration(); queuedSections.hasNextIteration(it); it = queuedSections.nextEntry(it)) {
                    long l = queuedSections.getIterationKey(it);
                    if (this.storingLightForSection(l)) {
                        queuedSections.remove(l);
                    }
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void queueSectionData(long pos, @Nullable DataLayer dataLayer, boolean bl) {
        if (dataLayer != null) {
            synchronized (this.queuedSections) {
                this.queuedSections.put(pos, dataLayer);
            }
            if (!bl) {
                this.untrustedSections.add(pos);
            }
        }
        else {
            synchronized (this.queuedSections) {
                this.queuedSections.remove(pos);
            }
        }
    }

    @Shadow
    protected abstract void clearQueuedSectionBlocks(LayerLightEngine<?, ?> layerLightEngine, long l);

    @Shadow
    protected abstract @Nullable DataLayer getDataLayer(long l, boolean bl);

    @Shadow
    protected abstract boolean hasInconsistencies();

    @Shadow
    protected abstract void onNodeRemoved(long l);

    @Shadow
    protected abstract boolean storingLightForSection(long l);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private void checkEdgesForSection(LayerLightEngine<M, ?> layerLightEngine, long pos) {
        if (this.storingLightForSection(pos)) {
            int x = SectionPos.sectionToBlockCoord(SectionPos.x(pos));
            int y = SectionPos.sectionToBlockCoord(SectionPos.y(pos));
            int z = SectionPos.sectionToBlockCoord(SectionPos.z(pos));
            for (Direction dir : DirectionUtil.ALL) {
                long m = SectionPos.offset(pos, dir);
                boolean doesntHaveKey;
                synchronized (this.queuedSections) {
                    doesntHaveKey = !this.queuedSections.containsKey(m);
                }
                if (doesntHaveKey && this.storingLightForSection(m)) {
                    for (int dz = 0; dz < 16; ++dz) {
                        for (int dx = 0; dx < 16; ++dx) {
                            long p;
                            long q = switch (dir) {
                                case DOWN -> {
                                    p = BlockPos.asLong(x + dx, y, z + dz);
                                    yield BlockPos.asLong(x + dx, y - 1, z + dz);
                                }
                                case UP -> {
                                    p = BlockPos.asLong(x + dx, y + 16 - 1, z + dz);
                                    yield BlockPos.asLong(x + dx, y + 16, z + dz);
                                }
                                case NORTH -> {
                                    p = BlockPos.asLong(x + dz, y + dx, z);
                                    yield BlockPos.asLong(x + dz, y + dx, z - 1);
                                }
                                case SOUTH -> {
                                    p = BlockPos.asLong(x + dz, y + dx, z + 16 - 1);
                                    yield BlockPos.asLong(x + dz, y + dx, z + 16);
                                }
                                case WEST -> {
                                    p = BlockPos.asLong(x, y + dz, z + dx);
                                    yield BlockPos.asLong(x - 1, y + dz, z + dx);
                                }
                                default -> {
                                    p = BlockPos.asLong(x + 16 - 1, y + dz, z + dx);
                                    yield BlockPos.asLong(x + 16, y + dz, z + dx);
                                }
                            };
                            layerLightEngine.checkEdge(p, q, layerLightEngine.computeLevelFromNeighbor(p, q, layerLightEngine.getLevel(p)), false);
                            layerLightEngine.checkEdge(q, p, layerLightEngine.computeLevelFromNeighbor(q, p, layerLightEngine.getLevel(q)), false);
                        }
                    }
                }
            }
        }
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;" +
                                                                    "toRemove:Lit/unimi/dsi/fastutil/longs/LongSet;", opcode = Opcodes.PUTFIELD))
    private void onInit0(LayerLightSectionStorage instance, LongSet value) {
        this.toRemove = new LHashSet();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;untrustedSections:Lit/unimi/dsi/fastutil/longs/LongSet;", opcode = Opcodes.PUTFIELD))
    private void onInit1(LayerLightSectionStorage instance, LongSet value) {
        this.untrustedSections = new LHashSet();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/lighting/LayerLightSectionStorage;queuedSections:Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;", opcode = Opcodes.PUTFIELD))
    private void onInit2(LayerLightSectionStorage instance, Long2ObjectMap<DataLayer> value) {
        this.queuedSections = new L2OHashMap<>();
    }
}
