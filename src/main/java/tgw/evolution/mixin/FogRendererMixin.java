package tgw.evolution.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.biome.BiomeManager;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;
import tgw.evolution.util.Vec3f;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

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

    /**
     * @author MGSchultz
     * <p>
     * Modify the fog color
     */
    @Overwrite
    public static void setupColor(ActiveRenderInfo camera, float partialTicks, ClientWorld world, int renderDistance, float worldDarkenAmount) {
        FluidState fluidstate = camera.getFluidInCamera();
        if (fluidstate.is(FluidTags.WATER)) {
            long i = Util.getMillis();
            int j = world.getBiome(new BlockPos(camera.getPosition())).getWaterFogColor();
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
            float f = MathHelper.clamp((i - biomeChangedTime) / 5_000.0F, 0.0F, 1.0F);
            float f1 = MathHelper.lerp(f, j1, k);
            float f2 = MathHelper.lerp(f, k1, l);
            float f3 = MathHelper.lerp(f, l1, i1);
            fogRed = f1 / 255.0F;
            fogGreen = f2 / 255.0F;
            fogBlue = f3 / 255.0F;
            if (targetBiomeFog != j) {
                targetBiomeFog = j;
                previousBiomeFog = MathHelper.floor(f1) << 16 | MathHelper.floor(f2) << 8 | MathHelper.floor(f3);
                biomeChangedTime = i;
            }
        }
        else if (fluidstate.is(FluidTags.LAVA)) {
            fogRed = 0.6F;
            fogGreen = 0.1F;
            fogBlue = 0.0F;
            biomeChangedTime = -1L;
        }
        else {
            float f4 = 0.25F + 0.75F * renderDistance / 32.0F;
            f4 = 1.0F - (float) Math.pow(f4, 0.25);
            Vec3f skyColor = EarthHelper.getSkyColor(world, camera.getBlockPosition(), partialTicks, ClientEvents.getInstance().getDimension());
            float skyRed = skyColor.x;
            float skyGreen = skyColor.y;
            float skyBlue = skyColor.z;
            Vector3d fogColor;
            if (ClientEvents.getInstance().getDimension() != null) {
                BiomeManager biomeManager = world.getBiomeManager();
                Vector3d samplePos = camera.getPosition().subtract(2, 2, 2).scale(0.25);
                float skyFogMult = ClientEvents.getInstance().getDimension().getSunBrightness(partialTicks);
                fogColor = CubicSampler.gaussianSampleVec3(samplePos,
                                                           (x, y, z) -> ClientEvents.getInstance()
                                                                                    .getDimension()
                                                                                    .getBrightnessDependentFogColor(Vector3d.fromRGB24(biomeManager.getNoiseBiomeAtQuart(
                                                                                            x,
                                                                                            y,
                                                                                            z).getFogColor()), skyFogMult));
            }
            else {
                fogColor = Vector3d.ZERO;
            }
            fogRed = (float) fogColor.x();
            fogGreen = (float) fogColor.y();
            fogBlue = (float) fogColor.z();
            fogRed += (skyRed - fogRed) * f4;
            fogGreen += (skyGreen - fogGreen) * f4;
            fogBlue += (skyBlue - fogBlue) * f4;
            float f14 = world.getRainLevel(partialTicks);
            if (f14 > 0.0F) {
                float f15 = 1.0F - f14 * 0.5F;
                fogRed *= f15;
                fogGreen *= f15;
                float f18 = 1.0F - f14 * 0.4F;
                fogBlue *= f18;
            }
            float f16 = world.getThunderLevel(partialTicks);
            if (f16 > 0.0F) {
                float f19 = 1.0F - f16 * 0.5F;
                fogRed *= f19;
                fogGreen *= f19;
                fogBlue *= f19;
            }
            biomeChangedTime = -1L;
        }
        double d0 = camera.getPosition().y * world.getLevelData().getClearColorScale();
        if (camera.getEntity() instanceof LivingEntity && ((LivingEntity) camera.getEntity()).hasEffect(Effects.BLINDNESS)) {
            int i2 = ((LivingEntity) camera.getEntity()).getEffect(Effects.BLINDNESS).getDuration();
            if (i2 < 20) {
                d0 *= 1.0F - i2 / 20.0F;
            }
            else {
                d0 = 0.0D;
            }
        }
        if (d0 < 1.0D && !fluidstate.is(FluidTags.LAVA)) {
            if (d0 < 0.0D) {
                d0 = 0.0D;
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
        if (fluidstate.is(FluidTags.WATER)) {
            float f6 = 0.0F;
            if (camera.getEntity() instanceof ClientPlayerEntity) {
                ClientPlayerEntity clientplayerentity = (ClientPlayerEntity) camera.getEntity();
                f6 = clientplayerentity.getWaterVision();
            }
            float f9 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            // Forge: fix MC-4647 and MC-10480
            if (Float.isInfinite(f9)) {
                f9 = Math.nextAfter(f9, 0.0);
            }
            fogRed = fogRed * (1.0F - f6) + fogRed * f9 * f6;
            fogGreen = fogGreen * (1.0F - f6) + fogGreen * f9 * f6;
            fogBlue = fogBlue * (1.0F - f6) + fogBlue * f9 * f6;
        }
        else if (camera.getEntity() instanceof LivingEntity && ((LivingEntity) camera.getEntity()).hasEffect(Effects.NIGHT_VISION)) {
            float f7 = GameRenderer.getNightVisionScale((LivingEntity) camera.getEntity(), partialTicks);
            float f10 = Math.min(1.0F / fogRed, Math.min(1.0F / fogGreen, 1.0F / fogBlue));
            // Forge: fix MC-4647 and MC-10480
            if (Float.isInfinite(f10)) {
                f10 = Math.nextAfter(f10, 0.0);
            }
            fogRed = fogRed * (1.0F - f7) + fogRed * f10 * f7;
            fogGreen = fogGreen * (1.0F - f7) + fogGreen * f10 * f7;
            fogBlue = fogBlue * (1.0F - f7) + fogBlue * f10 * f7;
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
