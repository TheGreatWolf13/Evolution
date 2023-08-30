package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchBlockRenderDispatcher;
import tgw.evolution.resources.IKeyedReloadListener;
import tgw.evolution.resources.ReloadListernerKeys;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.IRandom;

import java.util.Collection;
import java.util.List;
import java.util.Random;

@Mixin(BlockRenderDispatcher.class)
public abstract class Mixin_CF_BlockRenderDispatcher implements PatchBlockRenderDispatcher, IKeyedReloadListener {

    @Unique private static final List<ResourceLocation> DEPENDENCY = List.of(ReloadListernerKeys.MODELS);
    @Unique private final IRandom random_;
    @Mutable @Shadow @Final @RestoreFinal private BlockColors blockColors;
    @Mutable @Shadow @Final @RestoreFinal private BlockEntityWithoutLevelRenderer blockEntityRenderer;
    @Mutable @Shadow @Final @RestoreFinal private BlockModelShaper blockModelShaper;
    @Mutable @Shadow @Final @RestoreFinal private LiquidBlockRenderer liquidBlockRenderer;
    @Mutable @Shadow @Final @RestoreFinal private ModelBlockRenderer modelRenderer;
    @Shadow @Final @DeleteField private Random random;

    @ModifyConstructor
    public Mixin_CF_BlockRenderDispatcher(BlockModelShaper blockModelShaper, BlockEntityWithoutLevelRenderer blockEntityWithoutLevelRenderer, BlockColors blockColors) {
        this.random_ = new FastRandom();
        this.blockModelShaper = blockModelShaper;
        this.blockEntityRenderer = blockEntityWithoutLevelRenderer;
        this.blockColors = blockColors;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

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
    public boolean renderBatched(BlockState state, int x, int y, int z, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder, boolean checkSides, IRandom random) {
        try {
            return state.getRenderShape() == RenderShape.MODEL && this.modelRenderer.tesselateBlock(level, this.getBlockModel(state), state, x, y, z, matrices, builder, checkSides, random, state.getSeed_(x, y, z), OverlayTexture.NO_OVERLAY);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Tessellating block in world");
            CrashReportCategory category = crash.addCategory("Block being tessellated");
            category.setDetail("Block", state::toString);
            category.setDetail("Block location", () -> CrashReportCategory.formatLocation(level, x, y, z));
            throw new ReportedException(crash);
        }
    }

    @Overwrite
    public void renderBreakingTexture(BlockState state, BlockPos pos, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder) {
        Evolution.deprecatedMethod();
        this.renderBreakingTexture(state, pos.getX(), pos.getY(), pos.getZ(), level, matrices, builder);
    }

    @Override
    public void renderBreakingTexture(BlockState state, int x, int y, int z, BlockAndTintGetter level, PoseStack matrices, VertexConsumer builder) {
        if (state.getRenderShape() == RenderShape.MODEL) {
            this.modelRenderer.tesselateBlock(level, this.blockModelShaper.getBlockModel(state), state, x, y, z, matrices, builder, true, this.random_, state.getSeed_(x, y, z), OverlayTexture.NO_OVERLAY);
        }
    }

    @Overwrite
    public boolean renderLiquid(BlockPos pos, BlockAndTintGetter level, VertexConsumer builder, BlockState state, FluidState fluidState) {
        Evolution.deprecatedMethod();
        return this.renderLiquid(pos.getX(), pos.getY(), pos.getZ(), level, builder, state, fluidState);
    }

    @Override
    public boolean renderLiquid(int x, int y, int z, BlockAndTintGetter level, VertexConsumer builder, BlockState state, FluidState fluidState) {
        try {
            return this.liquidBlockRenderer.tesselate(level, x, y, z, builder, state, fluidState);
        }
        catch (Throwable t) {
            CrashReport crash = CrashReport.forThrowable(t, "Tesselating liquid in world");
            CrashReportCategory category = crash.addCategory("Block being tesselated");
            category.setDetail("Block", state::toString);
            category.setDetail("Block location", () -> CrashReportCategory.formatLocation(level, x, y, z));
            throw new ReportedException(crash);
        }
    }
}
