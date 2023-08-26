package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.events.ItemEvents;
import tgw.evolution.items.IEvolutionItem;

import java.util.List;
import java.util.Optional;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Widget {

    @Shadow public int height;
    @Shadow @Final public List<Widget> renderables;
    @Shadow public int width;
    @Shadow protected Font font;
    @Shadow protected ItemRenderer itemRenderer;
    @Shadow @Final private List<GuiEventListener> children;
    @Shadow @Final private List<NarratableEntry> narratables;
    @Unique private ItemStack tooltipStack = ItemStack.EMPTY;

    @Overwrite
    public <T extends GuiEventListener & NarratableEntry> T addWidget(T widget) {
        this.children.add(widget);
        this.narratables.add(widget);
        if (widget instanceof AbstractWidget w) {
            w.setScreen((Screen) (Object) this);
        }
        return widget;
    }

    @Overwrite
    public void clearWidgets() {
        this.renderables.clear();
        List<GuiEventListener> children = this.children;
        for (int i = 0, len = children.size(); i < len; ++i) {
            if (children.get(i) instanceof AbstractWidget w) {
                w.setScreen(null);
            }
        }
        children.clear();
        this.narratables.clear();
    }

    @Shadow
    public abstract List<Component> getTooltipFromItem(ItemStack itemStack);

    @Overwrite
    public void removeWidget(GuiEventListener widget) {
        if (widget instanceof Widget) {
            this.renderables.remove(widget);
        }
        if (widget instanceof NarratableEntry) {
            this.narratables.remove(widget);
        }
        this.children.remove(widget);
        if (widget instanceof AbstractWidget w) {
            w.setScreen(null);
        }
    }

    @Override
    @Overwrite
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        List<Widget> renderables = this.renderables;
        for (int i = 0, len = renderables.size(); i < len; ++i) {
            renderables.get(i).render(matrices, mouseX, mouseY, partialTicks);
        }
    }

    @Overwrite
    public void renderTooltip(PoseStack matrices, ItemStack stack, int mouseX, int mouseY) {
        this.tooltipStack = stack;
        this.renderTooltip(matrices, stack.getItem() instanceof IEvolutionItem ? List.of() : this.getTooltipFromItem(stack), stack.getTooltipImage(), mouseX, mouseY);
        this.tooltipStack = ItemStack.EMPTY;
    }

    @Overwrite
    public void renderTooltip(PoseStack matrices, List<Component> tooltip, Optional<TooltipComponent> image, int mouseX, int mouseY) {
        this.renderTooltipInternal(matrices, ItemEvents.gatherTooltipComponents(this.tooltipStack, tooltip, image, mouseX, this.width, this.font), mouseX, mouseY);
    }

    @Overwrite
    private void renderTooltipInternal(PoseStack matrices, List<ClientTooltipComponent> tooltip, int mouseX, int mouseY) {
        if (!tooltip.isEmpty()) {
            int sizeWidth = 0;
            int sizeHeight = tooltip.size() == 1 ? -2 : 0;
            for (int i = 0, len = tooltip.size(); i < len; i++) {
                ClientTooltipComponent c = tooltip.get(i);
                int width = tooltip.get(i).getWidth(this.font);
                if (width > sizeWidth) {
                    sizeWidth = width;
                }
                sizeHeight += c.getHeight();
            }
            int x = mouseX + 12;
            int y = mouseY - 12;
            if (x + sizeWidth > this.width) {
                x -= 28 + sizeWidth;
            }
            if (y + sizeHeight + 6 > this.height) {
                y = this.height - sizeHeight - 6;
            }
            matrices.pushPose();
            float oldBlitOffset = this.itemRenderer.blitOffset;
            this.itemRenderer.blitOffset = 400.0F;
            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            RenderSystem.setShader(RenderHelper.SHADER_POSITION_COLOR);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            Matrix4f matrix = matrices.last().pose();
            fillGradient(matrix, builder, x - 3, y - 4, x + sizeWidth + 3, y - 3, 400, 0xf010_0010, 0xf010_0010);
            fillGradient(matrix, builder, x - 3, y + sizeHeight + 3, x + sizeWidth + 3, y + sizeHeight + 4, 400, 0xf010_0010, 0xf010_0010);
            fillGradient(matrix, builder, x - 3, y - 3, x + sizeWidth + 3, y + sizeHeight + 3, 400, 0xf010_0010, 0xf010_0010);
            fillGradient(matrix, builder, x - 4, y - 3, x - 3, y + sizeHeight + 3, 400, 0xf010_0010, 0xf010_0010);
            fillGradient(matrix, builder, x + sizeWidth + 3, y - 3, x + sizeWidth + 4, y + sizeHeight + 3, 400, 0xf010_0010, 0xf010_0010);
            fillGradient(matrix, builder, x - 3, y - 3 + 1, x - 3 + 1, y + sizeHeight + 3 - 1, 400, 0x5050_00FF, 0x5050_00FF);
            fillGradient(matrix, builder, x + sizeWidth + 2, y - 3 + 1, x + sizeWidth + 3, y + sizeHeight + 3 - 1, 400, 0x5050_00FF, 0x5050_00FF);
            fillGradient(matrix, builder, x - 3, y - 3, x + sizeWidth + 3, y - 3 + 1, 400, 0x5050_00FF, 0x5050_00FF);
            fillGradient(matrix, builder, x - 3, y + sizeHeight + 2, x + sizeWidth + 3, y + sizeHeight + 3, 400, 0x5050_00FF, 0x5050_00FF);
            RenderSystem.enableDepthTest();
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            builder.end();
            BufferUploader.end(builder);
            RenderSystem.disableBlend();
            RenderSystem.enableTexture();
            MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            matrices.translate(0, 0, 400);
            int height = y;
            for (int i = 0, len = tooltip.size(); i < len; ++i) {
                ClientTooltipComponent c = tooltip.get(i);
                c.renderText(this.font, x, height, matrix, bufferSource);
                height += c.getHeight() + (i == 0 ? 2 : 0);
            }
            bufferSource.endBatch();
            matrices.popPose();
            height = y;
            for (int i = 0, len = tooltip.size(); i < len; ++i) {
                ClientTooltipComponent c = tooltip.get(i);
                c.renderImage(this.font, x, height, matrices, this.itemRenderer, 400);
                height += c.getHeight() + (i == 0 ? 2 : 0);
            }
            this.itemRenderer.blitOffset = oldBlitOffset;
        }
    }
}
