package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.MathHelper;

import java.util.Collection;
import java.util.Comparator;

public abstract class ScreenDisplayEffects<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected final Inventory inventory;
    private final Comparator<MobEffectInstance> comparator = (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getEffect()
                                                                                                              .getDisplayName()
                                                                                                              .getString(),
                                                                                                             b.getEffect()
                                                                                                              .getDisplayName()
                                                                                                              .getString());
    protected boolean hasActivePotionEffects;
    private int effectHeight;
    private @Nullable OList<MobEffectInstance> effects;
    private boolean full;
    private int initialHeight;
    private @Nullable OList<FormattedCharSequence> lines;
    private @Nullable OList<Component> tooltips;

    public ScreenDisplayEffects(T screenContainer, Inventory inv, Component title) {
        super(screenContainer, inv, title);
        this.inventory = inv;
    }

    public static int getFixedAmplifier(MobEffectInstance effect) {
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

    private static String getPotionDurationString(MobEffectInstance effect, float durationFactor) {
        if (effect.isNoCounter()) {
            return "\u221E";
        }
        int i = Mth.floor(effect.getDuration() * durationFactor);
        return StringUtil.formatTickDuration(i);
    }

    private void drawActivePotionEffects(PoseStack matrices) {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.effectHeight = 33;
            if (this.effects == null) {
                this.effects = new OArrayList<>();
            }
            else {
                this.effects.clear();
            }
            for (MobEffectInstance instance : collection) {
                if (instance.getDuration() > 0) {
                    this.effects.add(instance);
                }
            }
            this.effects.sort(this.comparator);
            int totalEffectHeight = 0;
            for (int i = 0, l = this.effects.size(); i < l; i++) {
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
            if (this.effects != null) {
                this.effects.clear();
            }
        }
    }

    private void drawActivePotionEffectsBackgrounds(PoseStack matrices) {
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
        int height = this.initialHeight;
        if (this.effects != null) {
            for (int i = 0, l = this.effects.size(); i < l; i++) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                this.blit(matrices, 2, height, 0, 180, 156, 32);
                height += this.effectHeight;
            }
        }
    }

    private void drawActivePotionEffectsIcons(PoseStack matrices) {
        assert this.minecraft != null;
        MobEffectTextureManager potionSprite = this.minecraft.getMobEffectTextures();
        int height = this.initialHeight;
        if (this.effects != null) {
            for (int i = 0, l = this.effects.size(); i < l; i++) {
                TextureAtlasSprite atlasSprite = potionSprite.get(this.effects.get(i).getEffect());
                RenderSystem.setShaderTexture(0, atlasSprite.atlas().location());
                blit(matrices, 8, height + 7, this.getBlitOffset(), 18, 18, atlasSprite);
                height += this.effectHeight;
            }
        }
    }

    private void drawActivePotionEffectsNames(PoseStack matrices) {
        int height = this.initialHeight;
        if (this.effects != null) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0, l = this.effects.size(); i < l; i++) {
                MobEffectInstance effect = this.effects.get(i);
                builder.setLength(0);
                builder.append(I18n.get(effect.getEffect().getDescriptionId()));
                int ampl = getFixedAmplifier(effect);
                if (ampl >= 1) {
                    builder.append(' ');
                    builder.append(MathHelper.getRomanNumber(ampl + 1));
                }
                this.font.drawShadow(matrices, builder.toString(), 30, height + 6, 0xff_ffff);
                this.font.drawShadow(matrices, getPotionDurationString(effect, 1.0F), 30, height + 16, 0x7f_7f7f);
                height += this.effectHeight;
            }
        }
    }

    public void drawActivePotionEffectsTooltips(PoseStack matrices, int mouseX, int mouseY, int leftOffset) {
        if (this.effects == null) {
            return;
        }
        int height = this.initialHeight;
        for (int ef = 0, l = this.effects.size(); ef < l; ef++) {
            MobEffectInstance effect = this.effects.get(ef);
            int h = this.effectHeight;
            if (this.full) {
                h -= 1;
            }
            if (ef == this.effects.size() - 1) {
                h = 32;
            }
            if (MathHelper.isMouseInRange(mouseX, mouseY, 2, height, Math.min(158, leftOffset), height + h)) {
                if (this.tooltips == null) {
                    this.tooltips = new OArrayList<>();
                }
                else {
                    this.tooltips.clear();
                }
                String amp = getFixedAmplifier(effect) > 0 ? " " + MathHelper.getRomanNumber(getFixedAmplifier(effect) + 1) : "";
                this.tooltips.add(new TranslatableComponent(effect.getEffect().getDescriptionId()).append(new TextComponent(amp))
                                                                                                  .withStyle(EvolutionStyles.DARK_AQUA));
                EvolutionEffects.getEffectDescription(this.tooltips, effect.getEffect(), getFixedAmplifier(effect));
                if (this.lines == null) {
                    this.lines = new OArrayList<>();
                }
                else {
                    this.lines.clear();
                }
                for (int i = 0, l1 = this.tooltips.size(); i < l1; i++) {
                    this.lines.addAll(this.font.split(this.tooltips.get(i), 250));
                }
                this.renderTooltip(matrices, this.lines, mouseX, mouseY);
                break;
            }
            height += this.effectHeight;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.updateActivePotionEffects();
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        if (this.hasActivePotionEffects && this.shouldDrawPotionEffects()) {
            this.drawActivePotionEffects(matrices);
        }
        super.render(matrices, mouseX, mouseY, partialTicks);
    }

    protected abstract boolean shouldDrawPotionEffects();

    public void updateActivePotionEffects() {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        if (this.minecraft.player.getActiveEffects().isEmpty()) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.hasActivePotionEffects = false;
        }
        else {
            this.hasActivePotionEffects = true;
        }
    }
}
