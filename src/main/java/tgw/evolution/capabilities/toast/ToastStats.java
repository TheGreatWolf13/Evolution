package tgw.evolution.capabilities.toast;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCToast;
import tgw.evolution.util.collection.IOpenHashSet;
import tgw.evolution.util.collection.ISet;
import tgw.evolution.util.toast.Toasts;

public class ToastStats implements IToastData {

    private final ISet unlocked = new IOpenHashSet();

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.unlocked.clear();
        int[] unlocked = nbt.getIntArray("Unlocked");
        for (int id : unlocked) {
            this.unlocked.add(id);
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("Unlocked", this.unlocked.toIntArray());
        return nbt;
    }

    @Override
    public void trigger(ServerPlayer player, ItemStack stack) {
        Item trigger = stack.getItem();
        int id = Toasts.getRecipeIdFor(trigger);
        if (id != -1) {
            if (!this.unlocked.contains(id)) {
                this.unlocked.add(id);
                EvolutionNetwork.send(player, new PacketSCToast(id));
            }
        }
    }
}
