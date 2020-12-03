package tgw.evolution.network;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import tgw.evolution.Evolution;

import java.util.function.Supplier;

public class PacketSCHandAnimation implements IPacket {

    private final Hand hand;

    public PacketSCHandAnimation(Hand hand) {
        this.hand = hand;
    }

    public static PacketSCHandAnimation decode(PacketBuffer buffer) {
        Hand hand = buffer.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
        return new PacketSCHandAnimation(hand);
    }

    public static void encode(PacketSCHandAnimation message, PacketBuffer buffer) {
        buffer.writeBoolean(message.hand == Hand.MAIN_HAND);
    }

    private static int getArmSwingAnimationEnd(PlayerEntity player) {
        if (EffectUtils.hasMiningSpeedup(player)) {
            return 6 - (1 + EffectUtils.getMiningSpeedup(player));
        }
        return player.isPotionActive(Effects.MINING_FATIGUE) ? 6 + (1 + player.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2 : 6;
    }

    public static void handle(PacketSCHandAnimation packet, Supplier<NetworkEvent.Context> context) {
        if (IPacket.checkSide(packet, context)) {
            context.get().enqueueWork(() -> swing(Evolution.PROXY.getClientPlayer(), packet.hand));
            context.get().setPacketHandled(true);
        }
    }

    private static void swing(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!stack.isEmpty() && stack.onEntitySwing(player)) {
            return;
        }
        if (!player.isSwingInProgress || player.swingProgressInt >= getArmSwingAnimationEnd(player) / 2 || player.swingProgressInt < 0) {
            player.swingProgressInt = -1;
            player.isSwingInProgress = true;
            player.swingingHand = hand;
        }
    }

    @Override
    public LogicalSide getDestinationSide() {
        return LogicalSide.CLIENT;
    }
}
