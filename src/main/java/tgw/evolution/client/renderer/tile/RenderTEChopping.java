package tgw.evolution.client.renderer.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import tgw.evolution.blocks.tileentities.TEChopping;
import tgw.evolution.init.EvolutionItems;

public class RenderTEChopping implements BlockEntityRenderer<TEChopping> {

    private static final ItemStack DESTROY_3 = new ItemStack(EvolutionItems.destroy_3.get());
    private static final ItemStack DESTROY_6 = new ItemStack(EvolutionItems.destroy_6.get());
    private static final ItemStack DESTROY_9 = new ItemStack(EvolutionItems.destroy_9.get());
    private final ItemRenderer itemRenderer;

    public RenderTEChopping(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(TEChopping tile, float partialTicks, PoseStack matrices, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!tile.hasLog()) {
            return;
        }
        ItemStack stack = tile.getItemStack();
        matrices.pushPose();
        matrices.translate(0.5, 0.75, 0.5);
        this.itemRenderer.renderStatic(stack, ItemTransforms.TransformType.FIXED, packedLight, packedOverlay, matrices, buffer, 0);
        switch (tile.getBreakProgress()) {
            case 1 -> this.itemRenderer.renderStatic(DESTROY_3, ItemTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer, 0);
            case 2 -> this.itemRenderer.renderStatic(DESTROY_6, ItemTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer, 0);
            case 3 -> this.itemRenderer.renderStatic(DESTROY_9, ItemTransforms.TransformType.FIXED, packedLight, packedLight, matrices, buffer, 0);
        }
        matrices.popPose();
    }
}
