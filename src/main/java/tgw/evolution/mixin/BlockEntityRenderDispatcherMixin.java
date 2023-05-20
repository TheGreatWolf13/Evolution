package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;

    @Shadow
    private static <T extends BlockEntity> void setupAndRender(BlockEntityRenderer<T> pRenderer,
                                                               T pBlockEntity,
                                                               float pPartialTick,
                                                               PoseStack pPoseStack, MultiBufferSource pBufferSource) {
    }

    @Shadow
    @Nullable
    public abstract <E extends BlockEntity> BlockEntityRenderer<E> getRenderer(E pBlockEntity);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
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
     * @author TheGreatWolf
     * @reason Avoid allocations
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
