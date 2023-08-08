package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.patches.PatchBlockRenderDispatcher;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Mixin(BlockRenderDispatcher.class)
public abstract class MixinBlockRenderDispatcher implements PatchBlockRenderDispatcher, IKeyedReloadListener {

    @Unique private static final List<ResourceLocation> DEPENDENCY = List.of(ReloadListernerKeys.MODELS);
    @Shadow @Final private BlockModelShaper blockModelShaper;
    @Shadow @Final private ModelBlockRenderer modelRenderer;
    @Shadow @Final private Random random;

    @Shadow
    public abstract BakedModel getBlockModel(BlockState blockState);

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return DEPENDENCY;
    }

    @Override
    public ResourceLocation getKey() {
        return ReloadListernerKeys.BLOCK_RENDERER;
    }

    /**
     * @author TheGreatWolf
     * @reason Don't use
     */
    @Overwrite
    public boolean renderBatched(BlockState blockState,
                                 BlockPos blockPos,
                                 BlockAndTintGetter blockAndTintGetter,
                                 PoseStack poseStack,
                                 VertexConsumer vertexConsumer,
                                 boolean bl,
                                 Random random) {
        Evolution.warn("wrong renderBatched called! Call the version with IModelData!");
        return false;
    }

    @Override
    public boolean renderBatched(BlockState state,
                                 BlockPos pos,
                                 BlockAndTintGetter level,
                                 PoseStack matrices,
                                 VertexConsumer builder,
                                 boolean checkSides,
                                 Random random,
                                 IModelData modelData) {
        try {
            RenderShape shape = state.getRenderShape();
            return shape == RenderShape.MODEL && this.modelRenderer.tesselateBlock(level, this.getBlockModel(state), state, pos, matrices, builder, checkSides, random, state.getSeed(pos), OverlayTexture.NO_OVERLAY, modelData);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Tessellating block in world");
            CrashReportCategory category = crash.addCategory("Block being tessellated");
            CrashReportCategory.populateBlockDetails(category, level, pos, state);
            throw new ReportedException(crash);
        }
    }

    /**
     * @reason Deprecated
     */
    @Overwrite
    public void renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            BakedModel bakedModel = this.blockModelShaper.getBlockModel(state);
            long l = state.getSeed(pos);
            this.modelRenderer.tesselateBlock(level, bakedModel, state, pos, matrices, builder, true, this.random, l, OverlayTexture.NO_OVERLAY, IModelData.EMPTY);
        }
    }
}
