package tgw.evolution.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.EffectRenderer;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.RenderProperties;
import tgw.evolution.client.util.ClientEffectInstance;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionResources;
import tgw.evolution.init.EvolutionStyles;
import tgw.evolution.util.math.MathHelper;

import java.util.Collection;
import java.util.Comparator;

@OnlyIn(Dist.CLIENT)
public abstract class ScreenDisplayEffects<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final int X = 2;
    protected final Inventory inventory;
    private final Comparator<MobEffectInstance> comparator = (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getEffect()
                                                                                                              .getDisplayName()
                                                                                                              .getString(),
                                                                                                             b.getEffect()
                                                                                                              .getDisplayName()
                                                                                                              .getString());
    protected boolean hasActivePotionEffects;
    private int effectHeight;
    private ObjectList<MobEffectInstance> effects;
    private boolean full;
    private int initialHeight;
    private ObjectList<FormattedCharSequence> lines;
    private ObjectList<Component> tooltips;

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
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.effectHeight = 33;
            if (this.effects == null) {
                this.effects = new ObjectArrayList<>();
            }
            else {
                this.effects.clear();
            }
            this.effects = collection.stream()
                                     .filter(effectInstance -> ForgeHooksClient.shouldRenderEffect(effectInstance) &&
                                                               effectInstance.getDuration() > 0)
                                     .sorted(this.comparator)
                                     .collect(() -> this.effects, ObjectList::add, ObjectList::addAll);
            int totalEffectHeight = 0;
            for (MobEffectInstance ignored : this.effects) {
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

    private void drawActivePotionEffectsBackgrounds(PoseStack matrices) {
        RenderSystem.setShaderTexture(0, EvolutionResources.GUI_INVENTORY);
        int i = this.initialHeight;
        for (MobEffectInstance ignored : this.effects) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(matrices, X, i, 0, 166, 156, 32);
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsIcons(PoseStack matrices) {
        MobEffectTextureManager potionSprite = this.minecraft.getMobEffectTextures();
        int i = this.initialHeight;
        for (MobEffectInstance effectinstance : this.effects) {
            MobEffect effect = effectinstance.getEffect();
            TextureAtlasSprite atlasSprite = potionSprite.get(effect);
            RenderSystem.setShaderTexture(0, atlasSprite.atlas().location());
            blit(matrices, X + 6, i + 7, this.getBlitOffset(), 18, 18, atlasSprite);
            i += this.effectHeight;
        }
    }

    private void drawActivePotionEffectsNames(PoseStack matrices) {
        int i = this.initialHeight;
        StringBuilder builder = new StringBuilder();
        for (MobEffectInstance effect : this.effects) {
            EffectRenderer renderer = RenderProperties.getEffectRenderer(effect);
            if (!renderer.shouldRenderInvText(effect)) {
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

    public void drawActivePotionEffectsTooltips(PoseStack matrices, int mouseX, int mouseY, int leftOffset) {
        if (this.effects == null) {
            return;
        }
        int i = this.initialHeight;
        for (int ef = 0; ef < this.effects.size(); ef++) {
            MobEffectInstance effect = this.effects.get(ef);
            int h = this.effectHeight;
            if (this.full) {
                h -= 1;
            }
            if (ef == this.effects.size() - 1) {
                h = 32;
            }
            if (MathHelper.isMouseInsideBox(mouseX, mouseY, X, i, Math.min(X + 156, leftOffset), i + h)) {
                if (this.tooltips == null) {
                    this.tooltips = new ObjectArrayList<>();
                }
                else {
                    this.tooltips.clear();
                }
                String amp = getFixedAmplifier(effect) > 0 ? " " + MathHelper.getRomanNumber(getFixedAmplifier(effect) + 1) : "";
                this.tooltips.add(new TranslatableComponent(effect.getEffect().getDescriptionId()).append(new TextComponent(amp))
                                                                                                  .setStyle(EvolutionStyles.EFFECTS));
                EvolutionEffects.getEffectDescription(this.tooltips, effect.getEffect(), getFixedAmplifier(effect));
                if (this.lines == null) {
                    this.lines = new ObjectArrayList<>();
                }
                else {
                    this.lines.clear();
                }
                for (Component comp : this.tooltips) {
                    this.lines.addAll(this.font.split(comp, 250));
                }
                this.renderTooltip(matrices, this.lines, mouseX, mouseY, this.font);
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
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
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
