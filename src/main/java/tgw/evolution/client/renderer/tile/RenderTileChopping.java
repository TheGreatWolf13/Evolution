package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.init.EvolutionItems;

public class RenderTileChopping extends TileEntityRenderer<TEChopping> {

    private static final ItemStack DESTROY_3 = new ItemStack(EvolutionItems.destroy_3.get());
    private static final ItemStack DESTROY_6 = new ItemStack(EvolutionItems.destroy_6.get());
    private static final ItemStack DESTROY_9 = new ItemStack(EvolutionItems.destroy_9.get());
    private final ItemRenderer itemRenderer;

    public RenderTileChopping() {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEChopping tile, double x, double y, double z, float partialTicks, int destroyStage) {
        if (!tile.hasLog()) {
            return;
        }
        ItemStack stack = tile.getItemStack();
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5f, (float) y + 0.75f, (float) z + 0.5f);
        this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        switch (tile.getBreakProgress()) {
            case 1:
                this.itemRenderer.renderItem(DESTROY_3, ItemCameraTransforms.TransformType.FIXED);
                break;
            case 2:
                this.itemRenderer.renderItem(DESTROY_6, ItemCameraTransforms.TransformType.FIXED);
                break;
            case 3:
                this.itemRenderer.renderItem(DESTROY_9, ItemCameraTransforms.TransformType.FIXED);
                break;
        }
        GlStateManager.popMatrix();
    }
}
