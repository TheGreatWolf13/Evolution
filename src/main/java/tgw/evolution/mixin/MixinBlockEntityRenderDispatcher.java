package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.renderer.ambient.DynamicLights;
import tgw.evolution.client.renderer.chunk.LevelRenderer;

import javax.annotation.Nullable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class MixinBlockEntityRenderDispatcher {

    @Shadow public Camera camera;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> renderer, T tile, float partialTick, PoseStack matrices, MultiBufferSource bufferSource) {
        Level level = tile.getLevel();
        int light;
        if (level != null) {
            BlockPos pos = tile.getBlockPos();
            light = LevelRenderer.getLightColor(level, pos.getX(), pos.getY(), pos.getZ());
        }
        else {
            light = DynamicLights.FULL_LIGHTMAP;
        }
        renderer.render(tile, partialTick, matrices, bufferSource, light, OverlayTexture.NO_OVERLAY);
    }

    @Shadow
    @Nullable
    public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E pBlockEntity);

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <E extends BlockEntity> void render(E tile, float partialTicks, PoseStack matrices, MultiBufferSource bufferSource) {
        BlockEntityRenderer<E> renderer = this.getRenderer(tile);
        if (renderer != null) {
            if (tile.hasLevel() && tile.getType().isValid(tile.getBlockState())) {
                if (renderer.shouldRender(tile, this.camera.getPosition())) {
                    try {
                        setupAndRender(renderer, tile, partialTicks, matrices, bufferSource);
                    }
                    catch (Throwable throwable) {
                        CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
                        CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
                        tile.fillCrashReportCategory(crashreportcategory);
                        throw new ReportedException(crashreport);
                    }
                }
            }
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public <E extends BlockEntity> boolean renderItem(E tile, PoseStack matrices, MultiBufferSource bufferSource, int light, int overlay) {
        BlockEntityRenderer<E> renderer = this.getRenderer(tile);
        if (renderer == null) {
            return true;
        }
        try {
            renderer.render(tile, 0, matrices, bufferSource, light, overlay);
        }
        catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Rendering Block Entity");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block Entity Details");
            tile.fillCrashReportCategory(crashreportcategory);
            throw new ReportedException(crashreport);
        }
        return false;
    }
}
