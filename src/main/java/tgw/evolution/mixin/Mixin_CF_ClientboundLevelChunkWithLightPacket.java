package tgw.evolution.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.physics.EarthHelper;

import java.util.BitSet;

@Mixin(ClientboundLevelChunkWithLightPacket.class)
public abstract class Mixin_CF_ClientboundLevelChunkWithLightPacket implements Packet<ClientGamePacketListener> {

    @Mutable @Shadow @Final @RestoreFinal private ClientboundLevelChunkPacketData chunkData;
    @Mutable @Shadow @Final @RestoreFinal private ClientboundLightUpdatePacketData lightData;
    @Mutable @Shadow @Final @RestoreFinal private int x;
    @Mutable @Shadow @Final @RestoreFinal private int z;

    @ModifyConstructor
    public Mixin_CF_ClientboundLevelChunkWithLightPacket(LevelChunk chunk, LevelLightEngine lightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2, boolean bl) {
        ChunkPos pos = chunk.getPos();
        this.x = EarthHelper.wrapChunkCoordinate(pos.x);
        this.z = EarthHelper.wrapChunkCoordinate(pos.z);
        this.chunkData = new ClientboundLevelChunkPacketData(chunk);
        this.lightData = new ClientboundLightUpdatePacketData(pos, lightEngine, bitSet, bitSet2, bl);
    }
}
