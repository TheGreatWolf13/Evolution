package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.EnumWoodVariant;

public class RenderTileChopping extends TileEntityRenderer<TEChopping> {

    private static final ItemStack DESTROY_3 = new ItemStack(EvolutionItems.destroy_3.get());
    private static final ItemStack DESTROY_6 = new ItemStack(EvolutionItems.destroy_6.get());
    private static final ItemStack DESTROY_9 = new ItemStack(EvolutionItems.destroy_9.get());
    private final ItemRenderer itemRenderer;

    public RenderTileChopping() {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEChopping tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage) {
        if (tileEntityIn.id == -1) {
            return;
        }
        ItemStack stack = new ItemStack(EnumWoodVariant.byId(tileEntityIn.id).getLog());
        GlStateManager.pushMatrix();
        GlStateManager.translatef((float) x + 0.5f, (float) y + 0.75f, (float) z + 0.5f);
        this.itemRenderer.renderItem(stack, ItemCameraTransforms.TransformType.FIXED);
        if (tileEntityIn.breakProgress == 1) {
            this.itemRenderer.renderItem(DESTROY_3, ItemCameraTransforms.TransformType.FIXED);
        }
        else if (tileEntityIn.breakProgress == 2) {
            this.itemRenderer.renderItem(DESTROY_6, ItemCameraTransforms.TransformType.FIXED);
        }
        else if (tileEntityIn.breakProgress == 3) {
            this.itemRenderer.renderItem(DESTROY_9, ItemCameraTransforms.TransformType.FIXED);
        }
        GlStateManager.popMatrix();
    }
}
