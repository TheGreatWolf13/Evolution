package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.patches.obj.IVec3dFetcher;
import tgw.evolution.util.earth.EarthHelper;
import tgw.evolution.util.math.Vec3d;
import tgw.evolution.util.math.Vec3f;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    private static final double[] GAUSSIAN_SAMPLE_KERNEL = {0, 1, 4, 6, 4, 1, 0};
    private static final Vec3d VEC = new Vec3d();

    @Shadow
    private static long biomeChangedTime;

    @Shadow
    private static int targetBiomeFog;

    @Shadow
    private static int previousBiomeFog;

    @Shadow
    private static float fogRed;

    @Shadow
    private static float fogGreen;

    @Shadow
    private static float fogBlue;

    private static Vec3d gaussianSampleVec3(double posX, double posY, double posZ, IVec3dFetcher fetcher) {
        int i = Mth.floor(posX);
        int j = Mth.floor(posY);
        int k = Mth.floor(posZ);
        double dx = posX - i;
        double dy = posY - j;
        double dz = posZ - k;
        double totalScale = 0;
        double vecX = 0;
        double vecY = 0;
        double vecZ = 0;
        Vec3d fetcherVec = VEC;
        for (int x = 0; x < 6; x++) {
            double scaleX = Mth.lerp(dx, GAUSSIAN_SAMPLE_KERNEL[x + 1], GAUSSIAN_SAMPLE_KERNEL[x]);
            int di = i - 2 + x;
            for (int y = 0; y < 6; y++) {
                double scaleY = Mth.lerp(dy, GAUSSIAN_SAMPLE_KERNEL[y + 1], GAUSSIAN_SAMPLE_KERNEL[y]);
                int dj = j - 2 + y;
                for (int z = 0; z < 6; z++) {
                    double scaleZ = Mth.lerp(dz, GAUSSIAN_SAMPLE_KERNEL[z + 1], GAUSSIAN_SAMPLE_KERNEL[z]);
                    int dk = k - 2 + z;
                    double scale = scaleX * scaleY * scaleZ;
                    totalScale += scale;
                    Vec3d fetch = fetcher.fetch(di, dj, dk, fetcherVec).scale(scale);
                    vecX += fetch.getX();
                    vecY += fetch.getY();
                    vecZ += fetch.getZ();
                }
            }
        }
        double finalScale = 1.0 / totalScale;
        vecX *= finalScale;
        vecY *= finalScale;
        vecZ *= finalScale;
        return fetcherVec.set(vecX, vecY, vecZ);
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Modify the fog color
     */
    @Overwrite
    public static void setupColor(Camera camera, float partialTicks, ClientLevel level, int renderDistance, float worldDarkenAmount) {
        FogType fogType = camera.getFluidInCamera();
        Entity entity = camera.getEntity();
        switch (fogType) {
            case WATER -> {
                long i = Util.getMillis();
                int j = level.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
                if (biomeChangedTime < 0L) {
                    targetBiomeFog = j;
                    previousBiomeFog = j;
                    biomeChangedTime = i;
                }
                int k = targetBiomeFog >> 16 & 255;
                int l = targetBiomeFog >> 8 & 255;
                int i1 = targetBiomeFog & 255;
                int j1 = previousBiomeFog >> 16 & 255;
                int k1 = previousBiomeFog >> 8 & 255;
                int l1 = previousBiomeFog & 255;
                float f = Mth.clamp((i - biomeChangedTime) / 5_000.0F, 0.0F, 1.0F);
                float f1 = Mth.lerp(f, j1, k);
                float f2 = Mth.lerp(f, k1, l);
                float f3 = Mth.lerp(f, l1, i1);
                fogRed = f1 / 255.0F;
                fogGreen = f2 / 255.0F;
                fogBlue = f3 / 255.0F;
                if (targetBiomeFog != j) {
                    targetBiomeFog = j;
                    previousBiomeFog = Mth.floor(f1) << 16 | Mth.floor(f2) << 8 | Mth.floor(f3);
                    biomeChangedTime = i;
                }
            }
            case LAVA -> {
                fogRed = 0.6F;
                fogGreen = 0.1F;
                fogBlue = 0.0F;
                biomeChangedTime = -1L;
            }
            case POWDER_SNOW -> {
                fogRed = 0.623F;
                fogGreen = 0.734F;
                fogBlue = 0.785F;
                biomeChangedTime = -1L;
                RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
            }
            default -> {
                float f4 = 0.25F + 0.75F * renderDistance / 32.0F;
                f4 = 1.0F - (float) Math.pow(f4, 0.25);
                Vec3f skyColor = EarthHelper.getSkyColor(level, camera.getBlockPosition(), partialTicks, ClientEvents.getInstance().getDimension());
                float skyRed = skyColor.x;
                float skyGreen = skyColor.y;
                float skyBlue = skyColor.z;
                Vec3d fogColor;
                if (ClientEvents.getInstance().getDimension() != null) {
                    BiomeManager biomeManager = level.getBiomeManager();
                    double posX = (camera.getPosition().x - 2) * 0.25;
                    double posY = (camera.getPosition().y - 2) * 0.25;
                    double posZ = (camera.getPosition().z - 2) * 0.25;
                    float skyFogMult = ClientEvents.getInstance().getDimension().getSunBrightness(partialTicks);
                    IVec3dFetcher fetcher = (x, y, z, vec) -> ClientEvents.getInstance()
                                                                          .getDimension()
                                                                          .getBrightnessDependentFogColor(Vec3d.fromRGB24(
                                                                                                                  biomeManager.getNoiseBiomeAtQuart(x, y, z).getFogColor(), vec),
                                                                                                          skyFogMult);
                    fogColor = gaussianSampleVec3(posX, posY, posZ, fetcher);
                }
                else {
                    fogColor = Vec3d.ZERO;
                }
                fogRed = (float) fogColor.getX();
                fogGreen = (float) fogColor.getY();
                fogBlue = (float) fogColor.getZ();
                fogRed += (skyRed - fogRed) * f4;
                fogGreen += (skyGreen - fogGreen) * f4;
                fogBlue += (skyBlue - fogBlue) * f4;
                float f14 = level.getRainLevel(partialTicks);
                if (f14 > 0.0F) {
                    float f15 = 1.0F - f14 * 0.5F;
                    fogRed *= f15;
                    fogGreen *= f15;
                    float f18 = 1.0F - f14 * 0.4F;
                    fogBlue *= f18;
                }
                float f16 = level.getThunderLevel(partialTicks);
                if (f16 > 0.0F) {
                    float f19 = 1.0F - f16 * 0.5F;
                    fogRed *= f19;
                    fogGreen *= f19;
                    fogBlue *= f19;
                }
                biomeChangedTime = -1L;
            }
        }
        double d0 = (camera.getPosition().y - level.getMinBuildHeight()) * level.getLevelData().getClearColorScale();
        if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.BLINDNESS)) {
            int i2 = living.getEffect(MobEffects.BLINDNESS).getDuration();
            if (i2 < 20) {
                d0 *= 1.0 - i2 / 20.0;
            }
            else {
                d0 = 0;
            }
        }
        if (d0 < 1 && fogType != FogType.LAVA) {
            if (d0 < 0) {
                d0 = 0;
            }
            d0 *= d0;
            fogRed *= d0;
            fogGreen *= d0;
            fogBlue *= d0;
        }
        if (worldDarkenAmount > 0.0F) {
            fogRed = fogRed * (1.0F - worldDarkenAmount) + fogRed * 0.7F * worldDarkenAmount;
            fogGreen = fogGreen * (1.0F - worldDarkenAmount) + fogGreen * 0.6F * worldDarkenAmount;
            fogBlue = fogBlue * (1.0F - worldDarkenAmount) + fogBlue * 0.6F * worldDarkenAmount;
        }
        float f6;
        if (fogType == FogType.WATER) {
            if (entity instanceof LocalPlayer player) {
                f6 = player.getWaterVision();
            }
            else {
                f6 = 1.0f;
            }
        }
        else if (entity instanceof LivingEntity living && living.hasEffect(MobEffects.NIGHT_VISION)) {
            f6 = GameRenderer.getNightVisionScale(living, partialTicks);
        }
        else {
            f6 = 0.0f;
        }
        if (fogRed != 0.0f && fogGreen != 0.0f && fogBlue != 0.0f) {
            float f8 = Math.min(1.0f / fogRed, Math.min(1.0f / fogGreen, 1.0f / fogBlue));
            // Forge: fix MC-4647 and MC-10480
            if (Float.isInfinite(f8)) {
                f8 = Math.nextAfter(f8, 0.0);
            }
            fogRed = fogRed * (1.0f - f6) + fogRed * f8 * f6;
            fogGreen = fogGreen * (1.0f - f6) + fogGreen * f8 * f6;
            fogBlue = fogBlue * (1.0f - f6) + fogBlue * f8 * f6;
        }
        EntityViewRenderEvent.FogColors event = new EntityViewRenderEvent.FogColors(camera, partialTicks, fogRed, fogGreen, fogBlue);
        MinecraftForge.EVENT_BUS.post(event);
        fogRed = event.getRed();
        fogGreen = event.getGreen();
        fogBlue = event.getBlue();
        if (ClientEvents.getInstance().getDimension() != null) {
            ClientEvents.getInstance().getDimension().setFogColor(fogRed, fogGreen, fogBlue);
        }
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }
}
