//package tgw.evolution.inventory.corpse;
//
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.entity.player.PlayerInventory;
//import net.minecraft.inventory.IInventory;
//import net.minecraft.inventory.container.Container;
//import net.minecraft.inventory.container.INamedContainerProvider;
//import net.minecraft.util.text.ITextComponent;
//import net.minecraft.util.text.TranslationTextComponent;
//
//import javax.annotation.Nullable;
//
//public class ContainerCorpseProvider implements INamedContainerProvider {
//
//    private final IInventory corpseInv;
//    private final String playerName;
//
//    public ContainerCorpseProvider(IInventory corpseInv, String playerName) {
//        this.corpseInv = corpseInv;
//        this.playerName = playerName;
//    }
//
//    @Override
//    public ITextComponent getDisplayName() {
//        return new TranslationTextComponent("evolution.container.corpse", this.playerName);
//    }
//
//    @Nullable
//    @Override
//    public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
//        return new ContainerCorpse(id, inventory, this.corpseInv);
//    }
//}
