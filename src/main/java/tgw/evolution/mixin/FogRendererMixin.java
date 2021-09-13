package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.CubicSampler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.events.ClientEvents;
import tgw.evolution.util.EarthHelper;
import tgw.evolution.util.MathHelper;

import javax.annotation.Nullable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSkyColor" +
                                                                         "(Lnet/minecraft/util/math/BlockPos;F)" +
                                                                         "Lnet/minecraft/util/math/vector/Vector3d;"))
    private static Vector3d setupColorProxy0(ClientWorld world, BlockPos pos, float partialTicks) {
        return EarthHelper.getSkyColor(world, pos, partialTicks, ClientEvents.getInstance().getDimension()).toVec3d();
    }

    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getTimeOfDay(F)F", ordinal = 0))
    private static float setupColorProxy1(ClientWorld world, float partialTicks) {
        return 0;
    }

    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getSunAngle(F)F"))
    private static float setupColorProxy2(ClientWorld world, float partialTicks) {
        float angle = 0;
        if (ClientEvents.getInstance().getDimension() != null) {
            angle = ClientEvents.getInstance().getDimension().getSunAngle() - 0.25f;
        }
        return MathHelper.wrapRadians(angle * MathHelper.TAU);
    }

    @Nullable
    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/DimensionRenderInfo;getSunriseColor(FF)[F"))
    private static float[] setupColorProxy3(DimensionRenderInfo dimensionRenderInfo, float celestialAngle, float partialTicks) {
        return null;
    }

    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/CubicSampler;gaussianSampleVec3" +
                                                                         "(Lnet/minecraft/util/math/vector/Vector3d;" +
                                                                         "Lnet/minecraft/util/CubicSampler$Vec3Fetcher;)" +
                                                                         "Lnet/minecraft/util/math/vector/Vector3d;"))
    private static Vector3d setupColorProxy4(Vector3d vec, CubicSampler.Vec3Fetcher fetcher) {
        if (ClientEvents.getInstance().getDimension() != null) {
            float f12 = MathHelper.clamp(MathHelper.cos(ClientEvents.getInstance().getDimension().getSunElevationAngle() / 372.0f * MathHelper.TAU) *
                                         2.0F + 0.5F, 0.0F, 1.0F);
            return CubicSampler.gaussianSampleVec3(vec,
                                                   (x, y, z) -> ClientEvents.getInstance()
                                                                            .getDimension()
                                                                            .biomeColorModifier(Vector3d.fromRGB24(Minecraft.getInstance().level.getBiomeManager()
                                                                                                                                                .getNoiseBiomeAtQuart(
                                                                                                                                                        x,
                                                                                                                                                        y,
                                                                                                                                                        z)
                                                                                                                                                .getFogColor()),
                                                                                                f12));
        }
        return CubicSampler.gaussianSampleVec3(vec, fetcher);
    }
}
