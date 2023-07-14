package tgw.evolution.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.ArrayUtils;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.L2OMap;

import java.util.List;
import java.util.Map;

@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class Mixin_CF_ClientboundLevelChunkPacketData {

    @Mutable @Shadow @Final @RestoreFinal private List<ClientboundLevelChunkPacketData.BlockEntityInfo> blockEntitiesData;
    @Mutable @Shadow @Final @RestoreFinal private byte[] buffer;
    @Mutable @Shadow @Final @RestoreFinal private CompoundTag heightmaps;

    @DummyConstructor
    public Mixin_CF_ClientboundLevelChunkPacketData() {
        this.blockEntitiesData = new OArrayList<>();
    }

    @ModifyConstructor
    public Mixin_CF_ClientboundLevelChunkPacketData(LevelChunk chunk) {
        this.heightmaps = new CompoundTag();
        Map<Heightmap.Types, Heightmap> hm = chunk.heightmaps_();
        for (Heightmap.Types types : ArrayUtils.HEIGHTMAP) {
            if (types.sendToClient()) {
                Heightmap heightmap = hm.get(types);
                if (heightmap != null) {
                    //noinspection ObjectAllocationInLoop
                    this.heightmaps.put(types.getSerializationKey(), new LongArrayTag(heightmap.getRawData()));
                }
            }
        }
        this.buffer = new byte[calculateChunkSize(chunk)];
        extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), chunk);
        this.blockEntitiesData = new OArrayList<>();
        L2OMap<BlockEntity> tes = chunk.blockEntities_();
        for (L2OMap.Entry<BlockEntity> e = tes.fastEntries(); e != null; e = tes.fastEntries()) {
            //noinspection ObjectAllocationInLoop
            this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create(e.value()));
        }
    }

    @Shadow
    private static int calculateChunkSize(LevelChunk levelChunk) {
        throw new AbstractMethodError();
    }

    @Shadow
    public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        throw new AbstractMethodError();
    }

    @Shadow
    protected abstract ByteBuf getWriteBuffer();
}
