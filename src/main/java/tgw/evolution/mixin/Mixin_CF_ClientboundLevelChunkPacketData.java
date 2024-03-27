package tgw.evolution.mixin;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DummyConstructor;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClientboundLevelChunkPacketData;
import tgw.evolution.patches.obj.IBlockEntityTagOutput;
import tgw.evolution.util.collection.ArrayHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.maps.L2OMap;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class Mixin_CF_ClientboundLevelChunkPacketData implements PatchClientboundLevelChunkPacketData {

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
        for (Heightmap.Types types : ArrayHelper.HEIGHTMAP) {
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
        for (long it = tes.beginIteration(); tes.hasNextIteration(it); it = tes.nextEntry(it)) {
            //noinspection ObjectAllocationInLoop
            this.blockEntitiesData.add(ClientboundLevelChunkPacketData.BlockEntityInfo.create(tes.getIterationValue(it)));
        }
    }

    @Shadow
    public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static int calculateChunkSize(LevelChunk levelChunk) {
        throw new AbstractMethodError();
    }

    @Override
    public Consumer<IBlockEntityTagOutput> getBlockEntitiesTagsConsumer_(int i, int j) {
        return tag -> this.getBlockEntitiesTags(tag, i, j);
    }

    @Shadow
    protected abstract ByteBuf getWriteBuffer();

    @Unique
    private void getBlockEntitiesTags(IBlockEntityTagOutput tag, int secX, int secZ) {
        int x0 = 16 * secX;
        int z0 = 16 * secZ;
        for (int i = 0, len = this.blockEntitiesData.size(); i < len; ++i) {
            ClientboundLevelChunkPacketData.BlockEntityInfo info = this.blockEntitiesData.get(i);
            int x = x0 + SectionPos.sectionRelative(info.packedXZ >> 4);
            int z = z0 + SectionPos.sectionRelative(info.packedXZ);
            tag.accept(x, info.y, z, info.type, info.tag);
        }
    }
}
