package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
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
    private float oldDarkenAmount = Float.NaN;
    private boolean oldForce;
    private float oldGamma = Float.NaN;
    private float oldNightVisionMod = Float.NaN;
    private float oldSkyBrightness = Float.NaN;
    private float oldSkyFlash = Float.NaN;

    public LightTextureEv(GameRenderer gameRenderer, Minecraft mc) {
        super(gameRenderer, mc);
        this.gameRenderer = gameRenderer;
        this.mc = mc;
        this.lightTexture = new DynamicTexture(1_024, 512, false);
        this.lightTextureLocation = this.mc.getTextureManager().register("light_map", this.lightTexture);
        NativeImage pixels = this.lightTexture.getPixels();
        assert pixels != null;
        this.lightPixels = pixels;
        for (int i = 0; i < 512; ++i) {
            for (int j = 0; j < 1_024; ++j) {
                this.lightPixels.setPixelRGBA(j, i, 0xffff_ffff);
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

    private static float[] getLightBrightnessTable(LevelReader level) {
        if (level.dimensionType().natural()) {
            DimensionOverworld dimension = ClientEvents.getInstance().getDimension();
            if (dimension != null) {
                return dimension.getLightBrightnessTable();
            }
        }
        return level.dimensionType().brightnessRamp;
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
        return 1.0F - f * f * f;
    }

    @Override
    public void close() {
        this.lightTexture.close();
    }

    @Override
    public void tick() {
        this.needsUpdate = true;
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
            ProfilerFiller profiler = this.mc.getProfiler();
            profiler.push("lightTex");
            ClientLevel level = this.mc.level;
            if (level != null) {
                boolean shouldUpdate = false;
                float skyBrightness = getSunBrightness(level, partialTicks);
                float skyFlash;
                if (level.getSkyFlashTime() > 0) {
                    skyFlash = 1.0F;
                }
                else {
                    skyFlash = skyBrightness;
                }
                if (!Mth.equal(skyFlash, this.oldSkyFlash)) {
                    this.oldSkyFlash = skyFlash;
                    shouldUpdate = true;
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
                if (nightVisionMod != this.oldNightVisionMod) {
                    this.oldNightVisionMod = nightVisionMod;
                    shouldUpdate = true;
                }
                float corrSkyBrightness = skyBrightness * 0.65f + 0.35f;
                if (!Mth.equal(corrSkyBrightness, this.oldSkyBrightness)) {
                    this.oldSkyBrightness = corrSkyBrightness;
                    shouldUpdate = true;
                }
                boolean forceBrightLightmap = level.effects().forceBrightLightmap();
                if (forceBrightLightmap != this.oldForce) {
                    this.oldForce = forceBrightLightmap;
                    shouldUpdate = true;
                }
                float darkenAmount = this.gameRenderer.getDarkenWorldAmount(partialTicks);
                if (darkenAmount != this.oldDarkenAmount) {
                    this.oldDarkenAmount = darkenAmount;
                    shouldUpdate = true;
                }
                float gamma = (float) this.mc.options.gamma;
                if (gamma != this.oldGamma) {
                    this.oldGamma = gamma;
                    shouldUpdate = true;
                }
                if (shouldUpdate) {
                    float skyRed = 1.0f;
                    float skyGreen = 1.0f;
                    float skyBlue = 1.0f;
                    float[] table = getLightBrightnessTable(level);
                    float invGamma = 1.0f - gamma;
                    for (int x = 0; x < 32; ++x) {
                        for (int y = 0; y < 32; ++y) {
                            for (int sl = 0; sl < 16; ++sl) {
                                for (int bl = 0; bl < 32; ++bl) {
                                    float skyLight = table[sl] * skyFlash;
                                    float bLRed = bl < 16 ? table[bl] * 0.5f : table[bl - 16];
                                    float bLGreen = x < 16 ? table[x] * 0.5f : table[x - 16];
                                    float bLBlue = y < 16 ? table[y] * 0.5f : table[y - 16];
                                    if (forceBrightLightmap) {
                                        bLRed *= 0.75f;
                                        bLGreen *= 0.75f;
                                        bLBlue *= 0.75f;
                                        bLRed += 0.99f * 0.25f;
                                        bLGreen += 1.12f * 0.25f;
                                        bLBlue += 0.25f;
                                    }
                                    else {
                                        float addend = corrSkyBrightness * skyLight;
                                        float redMod = addend * skyRed;
                                        float greenMod = addend * skyGreen;
                                        float blueMod = addend * skyBlue;
                                        bLRed *= 1 - redMod;
                                        bLGreen *= 1 - greenMod;
                                        bLBlue *= 1 - blueMod;
                                        bLRed += redMod;
                                        bLGreen += greenMod;
                                        bLBlue += blueMod;
                                        if (darkenAmount > 0.0F) {
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
                                    assert 0 <= bLRed && bLRed <= 1;
                                    assert 0 <= bLGreen && bLGreen <= 1;
                                    assert 0 <= bLBlue && bLBlue <= 1;
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
                                    float max = Math.max(bLRed, Math.max(bLGreen, bLBlue));
                                    float gammaMult = max == 0 ? 0 : (max * invGamma + invGamma(max) * gamma) / max;
                                    bLRed *= gammaMult;
                                    bLGreen *= gammaMult;
                                    bLBlue *= gammaMult;
                                    assert 0 <= bLRed && bLRed <= 1;
                                    assert 0 <= bLGreen && bLGreen <= 1;
                                    assert 0 <= bLBlue && bLBlue <= 1;
                                    bLRed *= 255.0f;
                                    bLGreen *= 255.0f;
                                    bLBlue *= 255.0f;
                                    int red = (int) bLRed;
                                    int green = (int) bLGreen;
                                    int blue = (int) bLBlue;
                                    this.lightPixels.setPixelRGBA(x * 32 + bl, y * 16 + sl, 0xff00_0000 | blue << 16 | green << 8 | red);
                                }
                            }
                        }
                    }
                    this.lightTexture.upload();
                }
            }
            profiler.pop();
        }
    }
}
