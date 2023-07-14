package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.GhostRecipe;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(GhostRecipe.class)
public abstract class MixinGhostRecipe {

    @Shadow float time;
    @Shadow @Final private List<GhostRecipe.GhostIngredient> ingredients;

    /**
     * @author TheGreatWolf
     * @reason Render ingredient count
     */
    @Overwrite
    public void render(PoseStack matrices, Minecraft mc, int leftPos, int topPos, boolean bigResultSlot, float partialTick) {
        if (!Screen.hasControlDown()) {
            this.time += partialTick;
        }
        for (int i = 0; i < this.ingredients.size(); i++) {
            GhostRecipe.GhostIngredient ingredient = this.ingredients.get(i);
            int x = ingredient.getX() + leftPos;
            int y = ingredient.getY() + topPos;
            if (i == 0 && bigResultSlot) {
                GuiComponent.fill(matrices, x - 4, y - 4, x + 20, y + 20, 0x30ff_0000);
            }
            else {
                GuiComponent.fill(matrices, x, y, x + 16, y + 16, 0x30ff_0000);
            }
            ItemStack stack = ingredient.getItem();
            ItemRenderer itemRenderer = mc.getItemRenderer();
            itemRenderer.renderAndDecorateFakeItem(stack, x, y);
            RenderSystem.depthFunc(GL11.GL_GREATER);
            GuiComponent.fill(matrices, x, y, x + 16, y + 16, 0x30ff_ffff);
            RenderSystem.depthFunc(GL11.GL_LEQUAL);
            itemRenderer.renderGuiItemDecorations(mc.font, stack, x, y);
        }
    }
}
