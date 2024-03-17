package tgw.evolution.mixin;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.patches.PatchClientGamePacketListener;

@Mixin(ClientGamePacketListener.class)
public interface MixinClientGamePacketListener extends PatchClientGamePacketListener, PacketListener {
}
