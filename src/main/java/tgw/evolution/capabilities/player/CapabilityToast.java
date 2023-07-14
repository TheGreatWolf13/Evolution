package tgw.evolution.capabilities.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.network.PacketSCToast;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.toast.Toasts;

public class CapabilityToast {

    private final ISet unlocked = new IHashSet();

    public void deserializeNBT(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        this.unlocked.clear();
        int[] unlocked = nbt.getIntArray("Unlocked");
        for (int id : unlocked) {
            this.unlocked.add(id);
        }
    }

    public void reset() {
        this.unlocked.clear();
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putIntArray("Unlocked", this.unlocked.toIntArray());
        return nbt;
    }

    public void set(CapabilityToast old) {
        this.unlocked.clear();
        this.unlocked.addAll(old.unlocked);
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        Item trigger = stack.getItem();
        int id = Toasts.getRecipeIdFor(trigger);
        if (id != -1) {
            if (!this.unlocked.contains(id)) {
                this.unlocked.add(id);
                player.connection.send(new PacketSCToast(id));
            }
        }
    }
}
