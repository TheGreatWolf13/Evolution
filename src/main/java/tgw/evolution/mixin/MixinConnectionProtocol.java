package tgw.evolution.mixin;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.init.EvolutionNetwork;

@Mixin(ConnectionProtocol.class)
public abstract class MixinConnectionProtocol {

    @SuppressWarnings({"InvalidMemberReference", "MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
    @Redirect(method = "<clinit>",
            at = @At(value = "NEW", target = "()Lnet/minecraft/network/ConnectionProtocol$PacketSet;", ordinal = 2))
    private static ConnectionProtocol.PacketSet<ServerGamePacketListener> onClinitC2S() {
        ConnectionProtocol.PacketSet<ServerGamePacketListener> set = new ConnectionProtocol.PacketSet();
        return EvolutionNetwork.registerC2S(set);
    }

    @SuppressWarnings({"InvalidMemberReference", "MixinAnnotationTarget", "InvalidInjectorMethodSignature"})
    @Redirect(method = "<clinit>",
            at = @At(value = "NEW", target = "()Lnet/minecraft/network/ConnectionProtocol$PacketSet;", ordinal = 1))
    private static ConnectionProtocol.PacketSet<ClientGamePacketListener> onClinitS2C() {
        ConnectionProtocol.PacketSet<ClientGamePacketListener> set = new ConnectionProtocol.PacketSet();
        return EvolutionNetwork.registerS2C(set);
    }
}
