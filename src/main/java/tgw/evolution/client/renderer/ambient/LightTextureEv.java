package tgw.evolution.client.renderer.ambient;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.potion.Effects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL13C;
import tgw.evolution.events.ClientEvents;

public class LightTextureEv extends LightTexture {

    private static final Vector3f LERP_0 = new Vector3f(1.0F, 1.0F, 1.0F);
    private static final Vector3f LERP_1 = new Vector3f(0.99F, 1.12F, 1.0F);
    private static final Vector3f VEC_0 = new Vector3f();
    private static final Vector3f VEC_1 = new Vector3f();
    private static final Vector3f VEC_2 = new Vector3f();
    private static final Vector3f VEC_3 = new Vector3f();
    private static final Vector3f VEC_4 = new Vector3f();
    private static final Vector3f VEC_5 = new Vector3f();
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
        this.lightPixels = this.lightTexture.getPixels();
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                this.lightPixels.setPixelRGBA(j, i, -1);
            }
        }
        this.lightTexture.upload();
    }

    private static float getLightBrightness(World world, int lightLevel) {
        if (world.dimensionType().natural()) {
            return ClientEvents.getInstance().getDimension().getAmbientLight(lightLevel);
        }
        return world.dimensionType().brightness(lightLevel);
    }

    private static float getSunBrightness(ClientWorld world, float partialTicks) {
        if (world.dimensionType().natural()) {
            return ClientEvents.getInstance().getDimension().getSunBrightness(partialTicks);
        }
        return world.getSkyDarken(partialTicks);
    }

    private static float invGamma(float value) {
        float f = 1.0F - value;
        return 1.0F - f * f * f * f;
    }

    private static void setVec(Vector3f ref, Vector3f assign) {
        ref.set(assign.x(), assign.y(), assign.z());
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
        RenderSystem.activeTexture(GL13C.GL_TEXTURE2);
        RenderSystem.disableTexture();
        RenderSystem.activeTexture(GL13C.GL_TEXTURE0);
    }

    @Override
    public void turnOnLightLayer() {
        RenderSystem.activeTexture(GL13C.GL_TEXTURE2);
        RenderSystem.matrixMode(GL11.GL_TEXTURE);
        RenderSystem.loadIdentity();
        RenderSystem.scalef(0.003_906_25F, 0.003_906_25F, 0.003_906_25F);
        RenderSystem.translatef(8.0F, 8.0F, 8.0F);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        this.mc.getTextureManager().bind(this.lightTextureLocation);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MIN_FILTER, 0x2601);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_MAG_FILTER, 0x2601);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_S, 0x2900);
        RenderSystem.texParameter(GL11C.GL_TEXTURE_2D, GL11C.GL_TEXTURE_WRAP_T, 0x2900);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableTexture();
        RenderSystem.activeTexture(GL13C.GL_TEXTURE0);
    }

    @Override
    public void updateLightTexture(float partialTicks) {
        if (this.needsUpdate) {
            this.needsUpdate = false;
            this.mc.getProfiler().push("lightTex");
            ClientWorld world = this.mc.level;
            if (world != null) {
                float skyBrightness = getSunBrightness(world, partialTicks);
                if (world.getSkyFlashTime() > 0) {
                    skyBrightness = 1.0F;
                }
                float waterBrightness = this.mc.player.getWaterVision();
                float nightVisionModifier;
                if (this.mc.player.hasEffect(Effects.NIGHT_VISION)) {
                    nightVisionModifier = GameRenderer.getNightVisionScale(this.mc.player, partialTicks);
                }
                else if (waterBrightness > 0.0F && this.mc.player.hasEffect(Effects.CONDUIT_POWER)) {
                    nightVisionModifier = waterBrightness;
                }
                else {
                    nightVisionModifier = 0.0F;
                }
                if (nightVisionModifier > 0) {
                    skyBrightness = 1.0f;
                }
                VEC_0.set(skyBrightness, skyBrightness, skyBrightness);
                VEC_0.lerp(LERP_0, 0.35F);
                float f4 = this.torchFlicker + 1.5F;
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x++) {
                        float skyLight = getLightBrightness(world, y) * skyBrightness;
                        float blockLight = getLightBrightness(world, x) * f4;
                        float f7 = blockLight * ((blockLight * 0.6F + 0.4F) * 0.6F + 0.4F);
                        float f8 = blockLight * (blockLight * blockLight * 0.6F + 0.4F);
                        VEC_1.set(blockLight, f7, f8);
                        if (world.effects().forceBrightLightmap()) {
                            VEC_1.lerp(LERP_1, 0.25F);
                        }
                        else {
                            setVec(VEC_2, VEC_0);
                            VEC_2.mul(skyLight);
                            VEC_1.add(VEC_2);
                            if (this.gameRenderer.getDarkenWorldAmount(partialTicks) > 0.0F) {
                                float f9 = this.gameRenderer.getDarkenWorldAmount(partialTicks);
                                setVec(VEC_3, VEC_1);
                                VEC_3.mul(0.7F, 0.6F, 0.6F);
                                VEC_1.lerp(VEC_3, f9);
                            }
                        }
                        VEC_1.clamp(0.0F, 1.0F);
                        if (nightVisionModifier > 0.0F) {
                            float f10 = Math.max(VEC_1.x(), Math.max(VEC_1.y(), VEC_1.z()));
                            if (f10 < 1.0F) {
                                setVec(VEC_5, VEC_1);
                                float f12 = 1.0F / f10;
                                VEC_5.mul(f12);
                                VEC_1.lerp(VEC_5, nightVisionModifier);
                            }
                        }
                        float gamma = (float) this.mc.options.gamma;
                        setVec(VEC_4, VEC_1);
                        VEC_4.map(LightTextureEv::invGamma);
                        VEC_1.lerp(VEC_4, gamma);
                        VEC_1.clamp(0.0F, 1.0F);
                        VEC_1.mul(255.0F);
                        int red = (int) VEC_1.x();
                        int green = (int) VEC_1.y();
                        int blue = (int) VEC_1.z();
                        this.lightPixels.setPixelRGBA(x, y, 0xff00_0000 | blue << 16 | green << 8 | red);
                    }
                }
                this.lightTexture.upload();
                this.mc.getProfiler().pop();
            }
        }
    }
}
