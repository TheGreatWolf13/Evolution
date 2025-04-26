package tgw.evolution.mixin;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.PatchTextureAtlasHolder;

@Mixin(TextureAtlasHolder.class)
public abstract class MixinTextureAtlasHolder extends SimplePreparableReloadListener<TextureAtlas.Preparations> implements AutoCloseable, PatchTextureAtlasHolder {

    @Shadow @Final private TextureAtlas textureAtlas;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public TextureAtlas.Preparations prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        profiler.startTick();
        profiler.push("stitching");
        TextureAtlas.Preparations preparations = this.textureAtlas.prepareToStitch(resourceManager, this.getResourcesToLoad_(this::resolveLocation).stream(), profiler, 0);
        profiler.pop();
        profiler.endTick();
        return preparations;
    }

    @Shadow
    protected abstract ResourceLocation resolveLocation(ResourceLocation resourceLocation);
}
