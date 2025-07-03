package tgw.evolution.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Collection;
import java.util.List;

@Mixin(ClientboundUpdateAttributesPacket.class)
public abstract class Mixin_CF_ClientboundUpdateAttributesPacket implements Packet<ClientGamePacketListener> {

    @Mutable @Shadow @Final @RestoreFinal private List<ClientboundUpdateAttributesPacket.AttributeSnapshot> attributes;
    @Mutable @Shadow @Final @RestoreFinal private int entityId;

    @ModifyConstructor
    public Mixin_CF_ClientboundUpdateAttributesPacket(int i, Collection<AttributeInstance> collection) {
        this.attributes = new OArrayList<>();
        if (collection instanceof OSet<AttributeInstance> set) {
            for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
                AttributeInstance attribute = set.getIteration(it);
                //noinspection ObjectAllocationInLoop
                this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute.getAttribute(), attribute.getBaseValue(), attribute.getModifiers()));
            }
        }
        else if (collection instanceof OList<AttributeInstance> list) {
            for (int j = 0, len = list.size(); j < len; ++j) {
                AttributeInstance attribute = list.get(j);
                //noinspection ObjectAllocationInLoop
                this.attributes.add(new ClientboundUpdateAttributesPacket.AttributeSnapshot(attribute.getAttribute(), attribute.getBaseValue(), attribute.getModifiers()));
            }
        }
        this.entityId = i;
    }
}
