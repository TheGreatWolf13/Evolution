package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.EnumMolding;
import tgw.evolution.client.renderer.RenderHelper;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSSetMoldingType;

import java.util.Map;

public class ScreenMolding extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 78;
    private final Object2ObjectMap<Button, ItemStack> buttons = new Object2ObjectOpenHashMap<>();
    private final BlockPos pos;
    private final ResourceLocation resBackground = Evolution.getResource("textures/gui/molding.png");
    private final ItemStack[] stacks;

    public ScreenMolding(BlockPos pos) {
        super(new TranslatableComponent("evolution.gui.molding"));
        this.pos = pos;
        this.stacks = new ItemStack[EnumMolding.VALUES.length - 1];
        for (int i = 0; i < this.stacks.length; i++) {
            this.stacks[i] = EnumMolding.VALUES[i + 1].getStack();
        }
    }

    public static void open(BlockPos pos) {
        Minecraft.getInstance().setScreen(new ScreenMolding(pos));
    }

    private void drawItemStack(ItemStack stack, int x, int y, @Nullable String altText) {
        PoseStack internalMat = RenderSystem.getModelViewStack();
        internalMat.translate(0, 0, 32);
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        this.itemRenderer.renderGuiItem(stack, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y, altText);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
        RenderSystem.applyModelViewMatrix();
    }

    @Override
    protected void init() {
        this.buttons.clear();
        int nButtons = this.stacks.length;
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        this.buttons.put(new Button(relX, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(EnumMolding.VALUES[1])), this.stacks[0]);
        this.buttons.put(new Button(relX + 25, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(EnumMolding.VALUES[2])), this.stacks[1]);
        for (Button button : this.buttons.keySet()) {
            this.addRenderableWidget(button);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        RenderSystem.setShader(RenderHelper.SHADER_POSITION_TEX);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, this.resBackground);
        int cornerX = (this.width - WIDTH) / 2;
        int cornerY = (this.height - HEIGHT) / 2;
        this.blit(matrices, cornerX, cornerY, 0, 0, WIDTH, HEIGHT);
        int nButtons = this.buttons.keySet().size();
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        drawCenteredString(matrices, this.font, this.title, this.width, cornerY + 5, 0x40_4040);
        super.render(matrices, mouseX, mouseY, partialTicks);
        for (int i = 0; i < this.stacks.length; i++) {
            this.drawItemStack(this.stacks[i], 2 + relX + 25 * i, 2 + relY, null);
        }
        for (Map.Entry<Button, ItemStack> entry : this.buttons.object2ObjectEntrySet()) {
            if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                this.renderTooltip(matrices, entry.getValue(), mouseX, mouseY);
            }
        }
    }

    private void setTile(EnumMolding type) {
        EvolutionNetwork.sendToServer(new PacketCSSetMoldingType(this.pos, type));
        assert this.minecraft != null;
        this.minecraft.setScreen(null);
    }
}
