package tgw.evolution.client.renderer.chunk;

import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import net.minecraft.util.profiling.metrics.MetricsSamplerProvider;
import net.minecraft.util.profiling.metrics.profiling.ProfilerSamplerAdapter;
import net.minecraft.util.profiling.metrics.profiling.ServerMetricsSamplersProvider;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Set;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

public class EvClientMetricsSamplersProvider implements MetricsSamplerProvider {
    private final EvLevelRenderer levelRenderer;
    private final ProfilerSamplerAdapter samplerFactory = new ProfilerSamplerAdapter();
    private final OSet<MetricSampler> samplers = new OHashSet<>();

    public EvClientMetricsSamplersProvider(LongSupplier ticker, EvLevelRenderer lvlRenderer) {
        this.levelRenderer = lvlRenderer;
        this.samplers.add(ServerMetricsSamplersProvider.tickTimeSampler(ticker));
        this.registerStaticSamplers();
    }

    private void registerStaticSamplers() {
        this.samplers.addAll(ServerMetricsSamplersProvider.runtimeIndependentSamplers());
        this.samplers.add(MetricSampler.create("totalChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, EvLevelRenderer::getTotalChunks));
        this.samplers.add(
                MetricSampler.create("renderedChunks", MetricCategory.CHUNK_RENDERING, this.levelRenderer, EvLevelRenderer::countRenderedChunks));
        this.samplers.add(
                MetricSampler.create("lastViewDistance", MetricCategory.CHUNK_RENDERING, this.levelRenderer, EvLevelRenderer::getLastViewDistance));
        EvChunkRenderDispatcher chunkrenderdispatcher = this.levelRenderer.getChunkRenderDispatcher();
        assert chunkrenderdispatcher != null;
        this.samplers.add(MetricSampler.create("toUpload", MetricCategory.CHUNK_RENDERING_DISPATCHING, chunkrenderdispatcher,
                                               EvChunkRenderDispatcher::getToUpload));
        this.samplers.add(MetricSampler.create("freeBufferCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, chunkrenderdispatcher,
                                               EvChunkRenderDispatcher::getFreeBufferCount));
        this.samplers.add(MetricSampler.create("toBatchCount", MetricCategory.CHUNK_RENDERING_DISPATCHING, chunkrenderdispatcher,
                                               EvChunkRenderDispatcher::getToBatchCount));
    }

    @Override
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> p_172544_) {
        this.samplers.addAll(this.samplerFactory.newSamplersFoundInProfiler(p_172544_));
        return this.samplers;
    }
}
