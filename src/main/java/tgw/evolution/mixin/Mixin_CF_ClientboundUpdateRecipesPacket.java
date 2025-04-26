package tgw.evolution.mixin;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.Collection;
import java.util.List;

@Mixin(ClientboundUpdateRecipesPacket.class)
public abstract class Mixin_CF_ClientboundUpdateRecipesPacket implements Packet<ClientGamePacketListener> {

    @Mutable @Shadow @Final @RestoreFinal private List<Recipe<?>> recipes;

    @ModifyConstructor
    public Mixin_CF_ClientboundUpdateRecipesPacket(Collection<Recipe<?>> collection) {
        this.recipes = new OArrayList<>(collection);
    }
}
