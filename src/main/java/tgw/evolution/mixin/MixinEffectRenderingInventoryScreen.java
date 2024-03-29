package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class MixinEffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public MixinEffectRenderingInventoryScreen(T abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void renderEffects(PoseStack matrices, int width, int height) {
        //I am not willing to do this
    }
}
