package tgw.evolution.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatKillPacket;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchClientboundPlayerCombatKillPacket;

@Mixin(ClientboundPlayerCombatKillPacket.class)
public abstract class Mixin_CF_ClientboundPlayerCombatKillPacket implements Packet<ClientGamePacketListener>, PatchClientboundPlayerCombatKillPacket {

    @Mutable @Shadow @Final @RestoreFinal private int killerId;
    @Mutable @Shadow @Final @RestoreFinal private Component message;
    @Mutable @Shadow @Final @RestoreFinal private int playerId;
    @Unique private long timeAlive;

    @ModifyConstructor
    public Mixin_CF_ClientboundPlayerCombatKillPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readVarInt();
        this.killerId = buf.readInt();
        this.message = buf.readComponent();
        this.timeAlive = buf.readVarLong();
    }

    @Override
    public long getTimeAlive() {
        return this.timeAlive;
    }

    @Override
    public ClientboundPlayerCombatKillPacket setTimeAlive(long timeAlive) {
        this.timeAlive = timeAlive;
        return (ClientboundPlayerCombatKillPacket) (Object) this;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.playerId);
        buf.writeInt(this.killerId);
        buf.writeComponent(this.message);
        buf.writeVarLong(this.timeAlive);
    }
}
