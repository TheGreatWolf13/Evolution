package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.tileentities.EnumKnapping;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketCSSetKnappingType;
import tgw.evolution.util.RockVariant;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ScreenKnapping extends Screen {

    private static final int WIDTH = 190;
    private static final int HEIGHT = 78;
    private static final ResourceLocation GUI = Evolution.getResource("textures/gui/knapping.png");
    private final Map<Button, ItemStack> buttons = new HashMap<>();
    private final BlockPos pos;
    private final ItemStack[] stacks = new ItemStack[EnumKnapping.values().length - 1];

    public ScreenKnapping(BlockPos pos, RockVariant variant) {
        super(new TranslationTextComponent("evolution.gui.knapping"));
        this.pos = pos;
        EnumKnapping[] values = EnumKnapping.values();
        for (int i = 0; i < this.stacks.length; i++) {
            //noinspection ObjectAllocationInLoop
            this.stacks[i] = variant.getKnappedStack(values[i + 1]);
        }
    }

    public static void open(BlockPos pos, RockVariant variant) {
        Minecraft.getInstance().displayGuiScreen(new ScreenKnapping(pos, variant));
    }

    private void drawItemStack(ItemStack stack, int x, int y, @Nullable String altText) {
        GlStateManager.translatef(0.0F, 0.0F, 32.0F);
        this.blitOffset = 200;
        this.itemRenderer.zLevel = 200.0F;
        this.itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRenderer.renderItemOverlayIntoGUI(this.font, stack, x, y, altText);
        this.blitOffset = 0;
        this.itemRenderer.zLevel = 0.0F;
    }

    @Override
    protected void init() {
        this.buttons.clear();
        int nButtons = this.stacks.length;
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        EnumKnapping[] values = EnumKnapping.values();
        this.buttons.put(new Button(relX, relY, 20, 20, "", button -> this.setTile(values[1])), this.stacks[0]);
        this.buttons.put(new Button(relX + 25, relY, 20, 20, "", button -> this.setTile(values[2])), this.stacks[1]);
        this.buttons.put(new Button(relX + 25 * 2, relY, 20, 20, "", button -> this.setTile(values[3])), this.stacks[2]);
        this.buttons.put(new Button(relX + 25 * 3, relY, 20, 20, "", button -> this.setTile(values[4])), this.stacks[3]);
        this.buttons.put(new Button(relX + 25 * 4, relY, 20, 20, "", button -> this.setTile(values[5])), this.stacks[4]);
        this.buttons.put(new Button(relX + 25 * 5, relY, 20, 20, "", button -> this.setTile(values[6])), this.stacks[5]);
        for (Button button : this.buttons.keySet()) {
            this.addButton(button);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        GlStateManager.color4f(1, 1, 1, 1);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int cornerX = (this.width - WIDTH) / 2;
        int cornerY = (this.height - HEIGHT) / 2;
        this.blit(cornerX, cornerY, 0, 0, WIDTH, HEIGHT);
        int nButtons = this.buttons.keySet().size();
        int xSize = 20 * nButtons + 5 * (nButtons - 1);
        int relX = (this.width - xSize) / 2;
        int relY = (this.height - 20) / 2;
        GUIUtils.drawCenteredString(this.font, this.title.getFormattedText(), this.width, cornerY + 5);
        super.render(mouseX, mouseY, partialTicks);
        for (int i = 0; i < this.stacks.length; i++) {
            this.drawItemStack(this.stacks[i], 2 + relX + 25 * i, 2 + relY, null);
        }
        for (Map.Entry<Button, ItemStack> entry : this.buttons.entrySet()) {
            if (entry.getKey().isMouseOver(mouseX, mouseY)) {
                this.renderTooltip(entry.getValue(), mouseX, mouseY);
            }
        }
    }

    private void setTile(EnumKnapping type) {
        EvolutionNetwork.INSTANCE.sendToServer(new PacketCSSetKnappingType(this.pos, type));
        this.minecraft.displayGuiScreen(null);
    }
}
