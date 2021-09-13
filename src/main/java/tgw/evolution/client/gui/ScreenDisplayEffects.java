package tgw.evolution.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.PotionSpriteUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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
import net.minecraftforge.fml.client.gui.GuiUtils;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.util.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public abstract class ScreenDisplayEffects<T extends Container> extends ContainerScreen<T> {
    private static final Comparator<EffectInstance> COMPARATOR = (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getEffect()
                                                                                                                  .getDisplayName()
                                                                                                                  .getString(),
                                                                                                                 b.getEffect()
                                                                                                                  .getDisplayName()
                                                                                                                  .getString());
    private static final int X = 2;
    private static final List<ITextComponent> TOOLTIPS = new ArrayList<>();
    protected boolean hasActivePotionEffects;
    private int effectHeight;
    private List<EffectInstance> effects;
    private boolean full;
    private int initialHeight;

    public ScreenDisplayEffects(T screenContainer, PlayerInventory inv, ITextComponent title) {
        super(screenContainer, inv, title);
    }

    public static int getFixedAmplifier(EffectInstance effect) {
        if (effect.getAmplifier() >= 0) {
            return effect.getAmplifier();
        }
        return Byte.toUnsignedInt((byte) effect.getAmplifier());
    }

    public static int getFixedAmplifier(ClientEffectInstance effect) {
        if (effect.getAmplifier() >= 0) {
            return effect.getAmplifier();
        }
        return Byte.toUnsignedInt((byte) effect.getAmplifier());
    }

    private static String getPotionDurationString(EffectInstance effect, float durationFactor) {
        if (effect.isNoCounter()) {
            return "\u221E";
        }
        int i = MathHelper.floor(effect.getDuration() * durationFactor);
        return StringUtils.formatTickDuration(i);
    }

    private void drawActivePotionEffects(MatrixStack matrices) {
        Collection<EffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableLighting();
            this.effectHeight = 33;
            this.effects = collection.stream()
                                     .filter(effectInstance -> effectInstance.getEffect().shouldRender(effectInstance) &&
                                                               effectInstance.getDuration() > 0)
                                     .sorted(COMPARATOR)
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
            this.drawActivePotionEffectsBackgrounds(matrices);
            this.drawActivePotionEffectsIcons(matrices);
            this.drawActivePotionEffectsNames(matrices);
        }
        else {
            this.effects.clear();
        }
    }

    private void drawActivePotionEffectsBackgrounds(MatrixStack matrices) {
        this.minecraft.getTextureManager().bind(EvolutionResources.GUI_INVENTORY);
        int i = this.initialHeight;
        for (EffectInstance ignored : this.effects) {
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(matrices, X, i, 0, 166, 140, 32);
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsIcons(MatrixStack matrices) {
        PotionSpriteUploader potionSprite = this.minecraft.getMobEffectTextures();
        int i = this.initialHeight;
        for (EffectInstance effectinstance : this.effects) {
            Effect effect = effectinstance.getEffect();
            TextureAtlasSprite atlasSprite = potionSprite.get(effect);
            this.minecraft.getTextureManager().bind(atlasSprite.atlas().location());
            blit(matrices, X + 6, i + 7, this.getBlitOffset(), 18, 18, atlasSprite);
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsNames(MatrixStack matrices) {
        int i = this.initialHeight;
        StringBuilder builder = new StringBuilder();
        for (EffectInstance effect : this.effects) {
            if (!effect.getEffect().shouldRenderInvText(effect)) {
                i += this.effectHeight;
                continue;
            }
            builder.setLength(0);
            builder.append(I18n.get(effect.getEffect().getDescriptionId()));
            int ampl = getFixedAmplifier(effect);
            if (ampl >= 1) {
                builder.append(' ');
                builder.append(MathHelper.getRomanNumber(ampl + 1));
            }
            this.font.drawShadow(matrices, builder.toString(), X + 28, i + 6, 0xff_ffff);
            this.font.drawShadow(matrices, getPotionDurationString(effect, 1.0F), X + 28, i + 16, 0x7f_7f7f);
            i += this.effectHeight;
        }
    }

    public void drawActivePotionEffectsTooltips(MatrixStack matrices, int mouseX, int mouseY, int leftOffset) {
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
                TOOLTIPS.add(new TranslationTextComponent(effect.getEffect().getDescriptionId()).append(new StringTextComponent(amp))
                                                                                                .setStyle(EvolutionStyles.EFFECTS));
                EvolutionEffects.getEffectDescription(TOOLTIPS, effect.getEffect(), getFixedAmplifier(effect));
                GuiUtils.drawHoveringText(matrices, TOOLTIPS, mouseX, mouseY, this.width, this.height, 250, this.font);
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.hasActivePotionEffects) {
            this.drawActivePotionEffects(matrices);
        }
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    public void updateActivePotionEffects() {
        if (this.minecraft.player.getActiveEffects().isEmpty()) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.hasActivePotionEffects = false;
        }
        else {
            this.hasActivePotionEffects = true;
        }
    }
}
