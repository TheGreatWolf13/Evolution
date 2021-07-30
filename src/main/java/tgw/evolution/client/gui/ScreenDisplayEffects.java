package tgw.evolution.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public abstract class ScreenDisplayEffects<T extends Container> extends ContainerScreen<T> {
    private static final int X = 2;
    private static final List<String> TOOLTIPS = new ArrayList<>();
    protected boolean hasActivePotionEffects;
    private int effectHeight;
    private List<EffectInstance> effects;
    private boolean full;
    private int initialHeight;

    public ScreenDisplayEffects(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    public static int getFixedAmplifier(EffectInstance effect) {
        if (effect.getAmplifier() >= 0) {
            return effect.getAmplifier();
        }
        return Byte.toUnsignedInt((byte) effect.getAmplifier());
    }

    private static String getPotionDurationString(EffectInstance effect, float durationFactor) {
        if (effect.getIsPotionDurationMax()) {
            return "\u221E";
        }
        int i = MathHelper.floor(effect.getDuration() * durationFactor);
        return StringUtils.ticksToElapsedTime(i);
    }

    private void drawActivePotionEffects() {
        Collection<EffectInstance> collection = this.minecraft.player.getActivePotionEffects();
        if (!collection.isEmpty()) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            this.effectHeight = 33;
            this.effects = collection.stream()
                                     .filter(effectInstance -> effectInstance.getPotion().shouldRender(effectInstance) &&
                                                               effectInstance.getDuration() > 0)
                                     .sorted()
                                     .collect(Collectors.toList());
            int totalEffectHeight = 0;
            for (EffectInstance ignored : this.effects) {
                totalEffectHeight += this.effectHeight;
            }
            this.initialHeight = (this.height - totalEffectHeight) / 2;
            this.full = totalEffectHeight + 20 > this.height;
            if (this.full) {
                this.initialHeight = 10;
                this.effectHeight = (this.height - 40) / (collection.size() - 1);
            }
            this.drawActivePotionEffectsBackgrounds();
            this.drawActivePotionEffectsIcons();
            this.drawActivePotionEffectsNames();
        }
        else {
            this.effects.clear();
        }
    }

    private void drawActivePotionEffectsBackgrounds() {
        this.minecraft.getTextureManager().bindTexture(EvolutionResources.GUI_INVENTORY);
        int i = this.initialHeight;
        for (EffectInstance ignored : this.effects) {
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(X, i, 0, 166, 140, 32);
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsIcons() {
        this.minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_EFFECTS_TEXTURE);
        PotionSpriteUploader potionspriteuploader = this.minecraft.getPotionSpriteUploader();
        int i = this.initialHeight;
        for (EffectInstance effectinstance : this.effects) {
            Effect effect = effectinstance.getPotion();
            blit(X + 6, i + 7, this.blitOffset, 18, 18, potionspriteuploader.getSprite(effect));
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsNames() {
        int i = this.initialHeight;
        StringBuilder builder = new StringBuilder();
        for (EffectInstance effect : this.effects) {
            if (!effect.getPotion().shouldRenderInvText(effect)) {
                i += this.effectHeight;
                continue;
            }
            builder.setLength(0);
            builder.append(I18n.format(effect.getPotion().getName()));
            if (getFixedAmplifier(effect) >= 1) {
                builder.append(' ');
                builder.append(MathHelper.getRomanNumber(getFixedAmplifier(effect) + 1));
            }
            this.font.drawStringWithShadow(builder.toString(), X + 28, i + 6, 0xff_ffff);
            this.font.drawStringWithShadow(getPotionDurationString(effect, 1.0F), X + 28, i + 16, 0x7f_7f7f);
            i += this.effectHeight;
        }
    }

    public void drawActivePotionEffectsTooltips(int mouseX, int mouseY, int leftOffset) {
        if (this.effects == null) {
            return;
        }
        int i = this.initialHeight;
        for (int ef = 0; ef < this.effects.size(); ef++) {
            EffectInstance effect = this.effects.get(ef);
            int h = this.effectHeight;
            if (this.full) {
                h -= 1;
            }
            if (ef == this.effects.size() - 1) {
                h = 32;
            }
            if (MathHelper.isMouseInsideBox(mouseX, mouseY, X, i, MathHelper.clampMax(X + 140, leftOffset), i + h)) {
                TOOLTIPS.clear();
                String amp = getFixedAmplifier(effect) > 0 ? " " + MathHelper.getRomanNumber(getFixedAmplifier(effect) + 1) : "";
                TOOLTIPS.add(new TranslationTextComponent(effect.getPotion().getName()).appendSibling(new StringTextComponent(amp))
                                                                                       .setStyle(EvolutionStyles.EFFECTS)
                                                                                       .getFormattedText());
                TOOLTIPS.add("");
                EvolutionEffects.getEffectDescription(TOOLTIPS, effect.getPotion(), getFixedAmplifier(effect));
                GUIUtils.renderTooltip(this, TOOLTIPS, mouseX, mouseY, 250);
                break;
            }
            i += this.effectHeight;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.updateActivePotionEffects();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (this.hasActivePotionEffects) {
            this.drawActivePotionEffects();
        }
        super.render(mouseX, mouseY, partialTicks);
    }

    public void updateActivePotionEffects() {
        if (this.minecraft.player.getActivePotionEffects().isEmpty()) {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
        else {
            this.hasActivePotionEffects = true;
        }
    }
}
