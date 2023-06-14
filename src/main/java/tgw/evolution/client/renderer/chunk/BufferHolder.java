package tgw.evolution.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.*;
import net.minecraft.client.resources.model.ModelBakery;
import tgw.evolution.util.constants.RenderLayer;

import java.util.Map;

public class BufferHolder {

    private final MultiBufferSource.BufferSource bufferSource;
    private final ChunkBuilderPack builderPack;
    private final MultiBufferSource.BufferSource crumblingBufferSource;
    private final OutlineBufferSource outlineBufferSource;

    public BufferHolder() {
        this.crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
        this.builderPack = new ChunkBuilderPack();
        Map<RenderType, BufferBuilder> fixedBuffers = new Object2ObjectLinkedOpenHashMap<>();
        fixedBuffers.put(Sheets.solidBlockSheet(), this.builderPack.builder(RenderLayer.SOLID));
        fixedBuffers.put(Sheets.cutoutBlockSheet(), this.builderPack.builder(RenderLayer.CUTOUT));
        fixedBuffers.put(Sheets.bannerSheet(), this.builderPack.builder(RenderLayer.CUTOUT_MIPPED));
        fixedBuffers.put(Sheets.translucentCullBlockSheet(), this.builderPack.builder(RenderLayer.TRANSLUCENT));
        put(fixedBuffers, Sheets.shieldSheet());
        put(fixedBuffers, Sheets.bedSheet());
        put(fixedBuffers, Sheets.shulkerBoxSheet());
        put(fixedBuffers, Sheets.signSheet());
        put(fixedBuffers, Sheets.chestSheet());
        put(fixedBuffers, RenderType.translucentNoCrumbling());
        put(fixedBuffers, RenderType.armorGlint());
        put(fixedBuffers, RenderType.armorEntityGlint());
        put(fixedBuffers, RenderType.glint());
        put(fixedBuffers, RenderType.glintDirect());
        put(fixedBuffers, RenderType.glintTranslucent());
        put(fixedBuffers, RenderType.entityGlint());
        put(fixedBuffers, RenderType.entityGlintDirect());
        put(fixedBuffers, RenderType.waterMask());
        for (int i = 0, len = ModelBakery.DESTROY_TYPES.size(); i < len; i++) {
            put(fixedBuffers, ModelBakery.DESTROY_TYPES.get(i));
        }
        this.bufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, new BufferBuilder(256));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
    }

    public BufferHolder(RenderBuffers buffers) {
        this.crumblingBufferSource = buffers.crumblingBufferSource();
        this.builderPack = new ChunkBuilderPack(buffers.fixedBufferPack());
        Map<RenderType, BufferBuilder> fixedBuffers = new Object2ObjectLinkedOpenHashMap<>();
        fixedBuffers.put(Sheets.solidBlockSheet(), this.builderPack.builder(RenderLayer.SOLID));
        fixedBuffers.put(Sheets.cutoutBlockSheet(), this.builderPack.builder(RenderLayer.CUTOUT));
        fixedBuffers.put(Sheets.bannerSheet(), this.builderPack.builder(RenderLayer.CUTOUT_MIPPED));
        fixedBuffers.put(Sheets.translucentCullBlockSheet(), this.builderPack.builder(RenderLayer.TRANSLUCENT));
        put(fixedBuffers, Sheets.shieldSheet());
        put(fixedBuffers, Sheets.bedSheet());
        put(fixedBuffers, Sheets.shulkerBoxSheet());
        put(fixedBuffers, Sheets.signSheet());
        put(fixedBuffers, Sheets.chestSheet());
        put(fixedBuffers, RenderType.translucentNoCrumbling());
        put(fixedBuffers, RenderType.armorGlint());
        put(fixedBuffers, RenderType.armorEntityGlint());
        put(fixedBuffers, RenderType.glint());
        put(fixedBuffers, RenderType.glintDirect());
        put(fixedBuffers, RenderType.glintTranslucent());
        put(fixedBuffers, RenderType.entityGlint());
        put(fixedBuffers, RenderType.entityGlintDirect());
        put(fixedBuffers, RenderType.waterMask());
        for (int i = 0, len = ModelBakery.DESTROY_TYPES.size(); i < len; i++) {
            put(fixedBuffers, ModelBakery.DESTROY_TYPES.get(i));
        }
        this.bufferSource = MultiBufferSource.immediateWithBuffers(fixedBuffers, new BufferBuilder(256));
        this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
    }

    private static void put(Map<RenderType, BufferBuilder> map, RenderType renderType) {
        map.put(renderType, new BufferBuilder(renderType.bufferSize()));
    }

    public MultiBufferSource.BufferSource bufferSource() {
        return this.bufferSource;
    }

    public ChunkBuilderPack chunkBuilderPack() {
        return this.builderPack;
    }

    public MultiBufferSource.BufferSource crumblingBufferSource() {
        return this.crumblingBufferSource;
    }

    public OutlineBufferSource outlineBufferSource() {
        return this.outlineBufferSource;
    }
}
