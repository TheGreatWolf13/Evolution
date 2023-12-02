package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.LevelReader;
import org.lwjgl.opengl.GL11C;
import tgw.evolution.client.renderer.DimensionOverworld;
import tgw.evolution.events.ClientEvents;

public class LightTextureEv extends LightTexture {

    private final GameRenderer gameRenderer;
    private final NativeImage lightPixels;
    private final DynamicTexture lightTexture;
    private final ResourceLocation lightTextureLocation;
    private final Minecraft mc;
    private boolean needsUpdate;
    private float torchFlicker;

    public LightTextureEv(GameRenderer gameRenderer, Minecraft mc) {
        super(gameRenderer, mc);
        this.gameRenderer = gameRenderer;
        this.mc = mc;
        this.lightTexture = new DynamicTexture(16, 16, false);
        this.lightTextureLocation = this.mc.getTextureManager().register("light_map", this.lightTexture);
        //noinspection ConstantConditions
        this.lightPixels = this.lightTexture.getPixels();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                //noinspection ConstantConditions
                this.lightPixels.setPixelRGBA(j, i, -1);
            }
        }
        this.lightTexture.upload();
    }

    public static float getLightBrightness(LevelReader level, int lightLevel) {
        if (level.dimensionType().natural()) {
            DimensionOverworld dimension = ClientEvents.getInstance().getDimension();
            if (dimension != null) {
                return dimension.getAmbientLight(lightLevel);
            }
        }
        return level.dimensionType().brightness(lightLevel);
    }

    private static float getSunBrightness(ClientLevel world, float partialTicks) {
        if (world.dimensionType().natural()) {
            assert ClientEvents.getInstance().getDimension() != null;
            return ClientEvents.getInstance().getDimension().getSkyBrightness(partialTicks);
        }
        return world.getSkyDarken(partialTicks);
    }

    private static float invGamma(float value) {
        float f = 1.0F - value;
        return 1.0F - f * f * f * f;
    }

    @Override
    public void close() {
        this.lightTexture.close();
    }

    @Override
    public void tick() {
        this.torchFlicker += (Math.random() - Math.random()) * Math.random() * Math.random() * 0.1;
        this.torchFlicker *= 0.9;
        this.needsUpdate = true;
    }

    @Override
    public void turnOffLightLayer() {
        RenderSystem.setShaderTexture(2, 0);
    }

    @Override
    public void turnOnLightLayer() {
        RenderSystem.setShaderTexture(2, this.lightTextureLocation);
        this.mc.getTextureManager().bindForSetup(this.lightTextureLocation);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, 0x2601);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, 0x2601);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void updateLightTexture(float partialTicks) {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            this.mc.getProfiler().push("lightTex");
            ClientLevel level = this.mc.level;
            if (level != null) {
                float skyBrightness = getSunBrightness(level, partialTicks);
                float skyFlash;
                if (level.getSkyFlashTime() > 0) {
                    skyFlash = 1.0F;
                }
                else {
                    skyFlash = skyBrightness;
                }
                assert this.mc.player != null;
                float waterBrightness = this.mc.player.getWaterVision();
                float nightVisionMod;
                if (this.mc.player.hasEffect(MobEffects.NIGHT_VISION)) {
                    nightVisionMod = GameRenderer.getNightVisionScale(this.mc.player, partialTicks);
                }
                else if (waterBrightness > 0.0F && this.mc.player.hasEffect(MobEffects.CONDUIT_POWER)) {
                    nightVisionMod = waterBrightness;
                }
                else {
                    nightVisionMod = 0.0F;
                }
                float corrSkyBrightness = skyBrightness * 0.65f + 0.35f;
                float flicker = this.torchFlicker + 1.5F;
                for (int sl = 0; sl < 16; sl++) {
                    for (int bl = 0; bl < 16; bl++) {
                        float skyLight = getLightBrightness(level, sl) * skyFlash;
                        float bLRed = getLightBrightness(level, bl) * flicker;
                        float bLGreen = bLRed * ((bLRed * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float bLBlue = bLRed * (bLRed * bLRed * 0.6F + 0.4F);
                        if (level.effects().forceBrightLightmap()) {
                            bLRed *= 0.75f;
                            bLGreen *= 0.75f;
                            bLBlue *= 0.75f;
                            bLRed += 0.99f * 0.25f;
                            bLGreen += 1.12f * 0.25f;
                            bLBlue += 0.25f;
                        }
                        else {
                            float addend = corrSkyBrightness * skyLight;
                            bLRed += addend;
                            bLGreen += addend;
                            bLBlue += addend;
                            if (this.gameRenderer.getDarkenWorldAmount(partialTicks) > 0.0F) {
                                float darkenAmount = this.gameRenderer.getDarkenWorldAmount(partialTicks);
                                float r = bLRed * 0.7f * darkenAmount;
                                float g = bLGreen * 0.6f * darkenAmount;
                                float b = bLBlue * 0.6f * darkenAmount;
                                float f = 1.0f - darkenAmount;
                                bLRed *= f;
                                bLGreen *= f;
                                bLBlue *= f;
                                bLRed += r;
                                bLGreen += g;
                                bLBlue += b;
                            }
                        }
                        if (bLRed > 1) {
                            bLRed = 1;
                        }
                        assert bLRed >= 0;
                        if (bLGreen > 1) {
                            bLGreen = 1;
                        }
                        assert bLGreen >= 0;
                        if (bLBlue > 1) {
                            bLBlue = 1;
                        }
                        assert bLBlue >= 0;
                        if (nightVisionMod > 0.0F) {
                            float max = Math.max(bLRed, Math.max(bLGreen, bLBlue));
                            if (max < 1.0F) {
                                float r = bLRed;
                                float g = bLGreen;
                                float b = bLBlue;
                                float f = 1.0F / max;
                                if (Float.isInfinite(f)) {
                                    r = 1.0f;
                                    g = 1.0f;
                                    b = 1.0f;
                                }
                                else {
                                    r *= f;
                                    g *= f;
                                    b *= f;
                                }
                                float m = 1 - nightVisionMod;
                                bLRed *= m;
                                bLGreen *= m;
                                bLBlue *= m;
                                bLRed += r * nightVisionMod;
                                bLGreen += g * nightVisionMod;
                                bLBlue += b * nightVisionMod;
                            }
                        }
                        float gamma = (float) this.mc.options.gamma;
                        float r = invGamma(bLRed) * gamma;
                        float g = invGamma(bLGreen) * gamma;
                        float b = invGamma(bLBlue) * gamma;
                        float f = 1.0f - gamma;
                        bLRed *= f;
                        bLGreen *= f;
                        bLBlue *= f;
                        bLRed += r;
                        bLGreen += g;
                        bLBlue += b;
                        assert 0 <= bLRed && bLRed <= 1;
                        assert 0 <= bLGreen && bLGreen <= 1;
                        assert 0 <= bLBlue && bLBlue <= 1;
                        bLRed *= 255.0f;
                        bLGreen *= 255.0f;
                        bLBlue *= 255.0f;
                        int red = (int) bLRed;
                        int green = (int) bLGreen;
                        int blue = (int) bLBlue;
                        this.lightPixels.setPixelRGBA(bl, sl, 0xff00_0000 | blue << 16 | green << 8 | red);
                    }
                }
                this.lightTexture.upload();
                this.mc.getProfiler().pop();
            }
        }
    }
}
