package tgw.evolution.capabilities.toast;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.PacketDistributor;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCToast;
import tgw.evolution.util.toast.Toasts;

public class ToastStats implements IToastData {

    private final IntSet unlocked = new IntOpenHashSet();

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.unlocked.clear();
        int[] unlocked = nbt.getIntArray("Unlocked");
        for (int id : unlocked) {
            this.unlocked.add(id);
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putIntArray("Unlocked", this.unlocked.toIntArray());
        return nbt;
    }

    @Override
    public void trigger(ServerPlayerEntity player, ItemStack stack) {
        Item trigger = stack.getItem();
        int id = Toasts.getRecipeIdFor(trigger);
        if (id != -1) {
            if (!this.unlocked.contains(id)) {
                this.unlocked.add(id);
                EvolutionNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new PacketSCToast(id));
            }
        }
    }
}
