package tgw.evolution.client.renderer.item;

import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import tgw.evolution.blocks.tileentities.TEShadowHound;
import tgw.evolution.init.EvolutionItems;

public class RenderStackTileShadowHound extends ItemStackTileEntityRenderer {

    private final TEShadowHound shadowHound = new TEShadowHound();

    @Override
    public void renderByItem(ItemStack stack) {
        if (stack.getItem() == EvolutionItems.shadowhound.get()) {
            TileEntityRendererDispatcher.instance.renderAsItem(this.shadowHound);
        }
    }
}
