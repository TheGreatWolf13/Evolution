package tgw.evolution.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SpriteCoordinateExpander;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.pipeline.*;

@Mixin(SpriteCoordinateExpander.class)
public abstract class SpriteCoordinateExpanderMixin implements IVertexDrain {

    @Shadow
    @Final
    private VertexConsumer delegate;
    @Shadow
    @Final
    private TextureAtlasSprite sprite;

    @Override
    public <T extends IVertexSink> T createSink(IVertexType<T> type) {
        if (type == VanillaVertexTypes.QUADS) {
            return (T) new SpriteTexturedVertexTransformer.Quad(IVertexDrain.of(this.delegate).createSink(VanillaVertexTypes.QUADS), this.sprite);
        }
        if (type == VanillaVertexTypes.PARTICLES) {
            return (T) new SpriteTexturedVertexTransformer.Particle(IVertexDrain.of(this.delegate).createSink(VanillaVertexTypes.PARTICLES),
                                                                    this.sprite);
        }
        if (type == VanillaVertexTypes.GLYPHS) {
            return (T) new SpriteTexturedVertexTransformer.Glyph(IVertexDrain.of(this.delegate).createSink(VanillaVertexTypes.GLYPHS), this.sprite);
        }
        return type.createFallbackWriter((VertexConsumer) this);
    }
}
