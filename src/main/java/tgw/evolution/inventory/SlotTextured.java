package tgw.evolution.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public class SlotTextured extends Slot {

    private final @Nullable Pair<ResourceLocation, ResourceLocation> icon;

    public SlotTextured(Container container, int i, int j, int k, @Nullable ResourceLocation icon) {
        super(container, i, j, k);
        if (icon != null) {
            this.icon = Pair.of(InventoryMenu.BLOCK_ATLAS, icon);
        }
        else {
            this.icon = null;
        }
    }

    @Override
    public @Nullable Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        return this.icon;
    }
}
