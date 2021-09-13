package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.blocks.tileentities.EnumMolding;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSSetMoldingType;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ScreenMolding extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 78;
    private static final ItemStack[] STACKS;
    private static final Map<Button, ItemStack> BUTTONS = new HashMap<>();

    static {
        EnumMolding[] values = EnumMolding.values();
        STACKS = new ItemStack[values.length - 1];
        for (int i = 0; i < STACKS.length; i++) {
            STACKS[i] = values[i + 1].getStack();
        }
    }

    private final BlockPos pos;

    public ScreenMolding(BlockPos pos) {
        super(new TranslationTextComponent("evolution.gui.molding"));
        this.pos = pos;
    }

    public static void open(BlockPos pos) {
        Minecraft.getInstance().setScreen(new ScreenMolding(pos));
    }

    private void drawItemStack(ItemStack stack, int x, int y, @Nullable String altText) {
        RenderSystem.translatef(0.0F, 0.0F, 32.0F);
        RenderHelper.turnBackOn();
        RenderSystem.disableLighting();
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        this.itemRenderer.renderGuiItem(stack, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y, altText);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
        RenderSystem.enableLighting();
        RenderHelper.turnOff();
    }

    @Override
    protected void init() {
        BUTTONS.clear();
        int nButtons = STACKS.length;
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        EnumMolding[] values = EnumMolding.values();
        BUTTONS.put(new Button(relX, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[1])), STACKS[0]);
        BUTTONS.put(new Button(relX + 25, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[2])), STACKS[1]);
        for (Button button : BUTTONS.keySet()) {
            this.addButton(button);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_MOLDING);
        int cornerX = (this.width - WIDTH) / 2;
        int cornerY = (this.height - HEIGHT) / 2;
        this.blit(matrices, cornerX, cornerY, 0, 0, WIDTH, HEIGHT);
        int nButtons = BUTTONS.keySet().size();
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        drawCenteredString(matrices, this.font, this.title, this.width, cornerY + 5, 0x40_4040);
        super.render(matrices, mouseX, mouseY, partialTicks);
        for (int i = 0; i < STACKS.length; i++) {
            this.drawItemStack(STACKS[i], 2 + relX + 25 * i, 2 + relY, null);
        }
        for (Map.Entry<Button, ItemStack> entry : BUTTONS.entrySet()) {
            if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                this.renderTooltip(matrices, entry.getValue(), mouseX, mouseY);
            }
        }
    }

    private void setTile(EnumMolding type) {
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetMoldingType(this.pos, type));
        this.minecraft.setScreen(null);
    }
}
