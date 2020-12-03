//package tgw.evolution.inventory.corpse;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.inventory.IInventory;
//import net.minecraft.inventory.container.Container;
//import net.minecraft.network.PacketBuffer;
//import tgw.evolution.init.EvolutionContainers;
//
//public class ContainerCorpse extends Container {
//
//    private final IInventory corpseInv;
//
//    public ContainerCorpse(int id, PlayerInventory playerInv, IInventory corpseInv) {
//        super(EvolutionContainers.CORPSE.get(), id);
//        this.corpseInv = corpseInv;
//    }
//
//    public ContainerCorpse(int id, PlayerInventory playerInv, PacketBuffer corpseInv) {
//        super(EvolutionContainers.CORPSE.get(), id);
//
//    }
//
//    @Override
//    public boolean canInteractWith(PlayerEntity player) {
//        return this.corpseInv.isUsableByPlayer(player);
//    }
//}
