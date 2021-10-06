package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.blocks.tileentities.KnappingRecipe;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionTexts;
import tgw.evolution.network.PacketCSSetKnappingType;
import tgw.evolution.util.RockVariant;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class ScreenKnapping extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 78;
    private final Map<Button, ItemStack> buttons = new HashMap<>();
    private final BlockPos pos;
    private final ItemStack[] stacks = new ItemStack[KnappingRecipe.VALUES.length - 1];

    public ScreenKnapping(BlockPos pos, RockVariant variant) {
        super(EvolutionTexts.GUI_KNAPPING);
        this.pos = pos;
        for (int i = 0; i < this.stacks.length; i++) {
            //noinspection ObjectAllocationInLoop
            this.stacks[i] = variant.getKnappedStack(KnappingRecipe.VALUES[i + 1]);
        }
    }

    public static void open(BlockPos pos, RockVariant variant) {
        Minecraft.getInstance().setScreen(new ScreenKnapping(pos, variant));
    }

    private void drawItemStack(ItemStack stack, int x, int y, @Nullable String altText) {
        RenderSystem.translatef(0.0f, 0.0f, 32.0f);
        this.setBlitOffset(200);
        this.itemRenderer.blitOffset = 200.0F;
        this.itemRenderer.renderGuiItem(stack, x, y);
        this.itemRenderer.renderGuiItemDecorations(this.font, stack, x, y, altText);
        this.setBlitOffset(0);
        this.itemRenderer.blitOffset = 0.0F;
    }

    @Override
    protected void init() {
        this.buttons.clear();
        int nButtons = this.stacks.length;
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        KnappingRecipe[] values = KnappingRecipe.VALUES;
        this.buttons.put(new Button(relX, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[1])), this.stacks[0]);
        this.buttons.put(new Button(relX + 25, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[2])), this.stacks[1]);
        this.buttons.put(new Button(relX + 25 * 2, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[3])), this.stacks[2]);
        this.buttons.put(new Button(relX + 25 * 3, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[4])), this.stacks[3]);
        this.buttons.put(new Button(relX + 25 * 4, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[5])), this.stacks[4]);
        this.buttons.put(new Button(relX + 25 * 5, relY, 20, 20, EvolutionTexts.EMPTY, button -> this.setTile(values[6])), this.stacks[5]);
        for (Button button : this.buttons.keySet()) {
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
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_KNAPPING);
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
        for (Map.Entry<Button, ItemStack> entry : this.buttons.entrySet()) {
            if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                this.renderTooltip(matrices, entry.getValue(), mouseX, mouseY);
            }
        }
    }

    private void setTile(KnappingRecipe type) {
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetKnappingType(this.pos, type));
        this.minecraft.setScreen(null);
    }
}
