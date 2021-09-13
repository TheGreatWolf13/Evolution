package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.init.EvolutionItems;

public class RenderTEChopping extends TileEntityRenderer<TEChopping> {

    private static final ItemStack DESTROY_3 = new ItemStack(EvolutionItems.destroy_3.get());
    private static final ItemStack DESTROY_6 = new ItemStack(EvolutionItems.destroy_6.get());
    private static final ItemStack DESTROY_9 = new ItemStack(EvolutionItems.destroy_9.get());
    private final ItemRenderer itemRenderer;

    public RenderTEChopping(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEChopping tile, float partialTicks, MatrixStack matrices, IRenderTypeBuffer buffer, int packedLight, int packedOverlay) {
        if (!tile.hasLog()) {
            return;
        }
        ItemStack stack = tile.getItemStack();
        matrices.pushPose();
        matrices.translate(0.5, 0.75, 0.5);
        this.itemRenderer.renderStatic(stack, ItemCameraTransforms.TransformType.FIXED, packedLight, packedOverlay, matrices, buffer);
        switch (tile.getBreakProgress()) {
            case 1: {
                this.itemRenderer.renderStatic(DESTROY_3, ItemCameraTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer);
                break;
            }
            case 2: {
                this.itemRenderer.renderStatic(DESTROY_6, ItemCameraTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer);
                break;
            }
            case 3: {
                this.itemRenderer.renderStatic(DESTROY_9, ItemCameraTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer);
                break;
            }
        }
        matrices.popPose();
    }
}
