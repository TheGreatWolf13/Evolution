package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import tgw.evolution.util.MathHelper;

public class LightTextureEv extends LightTexture {

    private static final float[] COLORS = new float[3];
    private final DynamicTexture dynamicTexture;
    private final NativeImage nativeImage;
    private final ResourceLocation resourceLocation;
    private final GameRenderer gameRenderer;
    private final Minecraft mc;
    private boolean needsUpdate;
    private float torchFlickerDX;
    private float torchFlickerX;

    public LightTextureEv(GameRenderer gameRenderer) {
        super(gameRenderer);
        this.gameRenderer = gameRenderer;
        this.mc = gameRenderer.getMinecraft();
        this.dynamicTexture = new DynamicTexture(16, 16, false);
        this.resourceLocation = this.mc.getTextureManager().getDynamicTextureLocation("light_map", this.dynamicTexture);
        this.nativeImage = this.dynamicTexture.getTextureData();
    }

    @Override
    public void close() {
        this.dynamicTexture.close();
    }

    @Override
    public void disableLightmap() {
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.disableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    @Override
    public void enableLightmap() {
        GlStateManager.activeTexture(GLX.GL_TEXTURE1);
        GlStateManager.matrixMode(5_890);
        GlStateManager.loadIdentity();
        GlStateManager.scalef(0.003_906_25F, 0.003_906_25F, 0.003_906_25F);
        GlStateManager.translatef(8.0F, 8.0F, 8.0F);
        GlStateManager.matrixMode(5_888);
        this.mc.getTextureManager().bindTexture(this.resourceLocation);
        GlStateManager.texParameter(3_553, 10_241, 9_729);
        GlStateManager.texParameter(3_553, 10_240, 9_729);
        GlStateManager.texParameter(3_553, 10_242, 10_496);
        GlStateManager.texParameter(3_553, 10_243, 10_496);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableTexture();
        GlStateManager.activeTexture(GLX.GL_TEXTURE0);
    }

    @Override
    public void tick() {
        this.torchFlickerDX += (Math.random() - Math.random()) * Math.random() * Math.random();
        this.torchFlickerDX *= 0.9;
        this.torchFlickerX += this.torchFlickerDX - this.torchFlickerX;
        this.needsUpdate = true;
    }

    @Override
    public void updateLightmap(float partialTicks) {
        if (this.needsUpdate) {
            this.mc.getProfiler().startSection("lightTex");
            World world = this.mc.world;
            if (world != null) {
                float skyBrightness = world.getSunBrightness(1.0F);
                float waterBrightness = this.mc.player.getWaterBrightness();
                float nightVisionBrightness;
                if (this.mc.player.isPotionActive(Effects.NIGHT_VISION)) {
                    nightVisionBrightness = this.gameRenderer.getNightVisionBrightness(this.mc.player, partialTicks);
                }
                else if (waterBrightness > 0.0F && this.mc.player.isPotionActive(Effects.CONDUIT_POWER)) {
                    nightVisionBrightness = waterBrightness;
                }
                else {
                    nightVisionBrightness = 0.0F;
                }
                if (nightVisionBrightness > 0) {
                    skyBrightness = 1.0f;
                }
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        float skyLight = world.dimension.getLightBrightnessTable()[i] * skyBrightness;
                        float blockLight = world.dimension.getLightBrightnessTable()[j] * (this.torchFlickerX * 0.1F + 1.5F);
                        if (world.getLastLightningBolt() > 0) {
                            skyLight = world.dimension.getLightBrightnessTable()[i];
                        }
                        float f6 = skyLight * (skyBrightness * 0.65F + 0.35F);
                        float f7 = skyLight * (skyBrightness * 0.65F + 0.35F);
                        float f9 = blockLight * (blockLight * blockLight * 0.6F + 0.4F);
                        float f8 = blockLight * ((blockLight * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float colorR = f6 + blockLight;
                        float colorG = f7 + f8;
                        float colorB = skyLight + f9;
                        if (this.gameRenderer.getBossColorModifier(partialTicks) > 0.0F) {
                            float f13 = this.gameRenderer.getBossColorModifier(partialTicks);
                            colorR = colorR * (1.0F - f13) + colorR * 0.7F * f13;
                            colorG = colorG * (1.0F - f13) + colorG * 0.6F * f13;
                            colorB = colorB * (1.0F - f13) + colorB * 0.6F * f13;
                        }
                        if (world.dimension.getType() == DimensionType.THE_END) {
                            colorR = 0.22F + blockLight * 0.75F;
                            colorG = 0.28F + f8 * 0.75F;
                            colorB = 0.25F + f9 * 0.75F;
                        }
                        COLORS[0] = colorR;
                        COLORS[1] = colorG;
                        COLORS[2] = colorB;
                        world.getDimension().getLightmapColors(partialTicks, skyBrightness, skyLight, blockLight, COLORS);
                        colorR = COLORS[0];
                        colorG = COLORS[1];
                        colorB = COLORS[2];
                        // Forge: fix MC-58177
                        colorR = MathHelper.clamp(colorR, 0.0f, 1.0f);
                        colorG = MathHelper.clamp(colorG, 0.0f, 1.0f);
                        colorB = MathHelper.clamp(colorB, 0.0f, 1.0f);
                        if (nightVisionBrightness > 0.0F) {
                            float f17 = 1.0F / colorR;
                            if (f17 > 1.0F / colorG) {
                                f17 = 1.0F / colorG;
                            }
                            if (f17 > 1.0F / colorB) {
                                f17 = 1.0F / colorB;
                            }
                            colorR = colorR * (1.0F - nightVisionBrightness) + colorR * f17 * nightVisionBrightness;
                            colorG = colorG * (1.0F - nightVisionBrightness) + colorG * f17 * nightVisionBrightness;
                            colorB = colorB * (1.0F - nightVisionBrightness) + colorB * f17 * nightVisionBrightness;
                        }
                        if (colorR > 1.0F) {
                            colorR = 1.0F;
                        }
                        if (colorG > 1.0F) {
                            colorG = 1.0F;
                        }
                        if (colorB > 1.0F) {
                            colorB = 1.0F;
                        }
                        float gamma = (float) this.mc.gameSettings.gamma;
                        float f14 = 1.0F - colorR;
                        float f15 = 1.0F - colorG;
                        float f16 = 1.0F - colorB;
                        f14 = 1.0F - f14 * f14 * f14 * f14;
                        f15 = 1.0F - f15 * f15 * f15 * f15;
                        f16 = 1.0F - f16 * f16 * f16 * f16;
                        colorR = colorR * (1.0F - gamma) + f14 * gamma;
                        colorG = colorG * (1.0F - gamma) + f15 * gamma;
                        colorB = colorB * (1.0F - gamma) + f16 * gamma;
                        colorR = MathHelper.clamp(colorR, 0, 1);
                        colorG = MathHelper.clamp(colorG, 0, 1);
                        colorB = MathHelper.clamp(colorB, 0, 1);
                        if (nightVisionBrightness > 0) {
                            colorR = 1;
                            colorG = 1;
                            colorB = 1;
                        }
                        int l = (int) (colorR * 255.0F);
                        int i1 = (int) (colorG * 255.0F);
                        int j1 = (int) (colorB * 255.0F);
                        //noinspection InsertLiteralUnderscores
                        this.nativeImage.setPixelRGBA(j, i, -16777216 | j1 << 16 | i1 << 8 | l);
                    }
                }
                this.dynamicTexture.updateDynamicTexture();
                this.needsUpdate = false;
                this.mc.getProfiler().endSection();
            }
        }
    }
}
