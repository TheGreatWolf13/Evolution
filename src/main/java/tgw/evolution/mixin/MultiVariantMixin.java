package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.models.ComplexWeightedModel;
import tgw.evolution.client.models.SimpleWeightedModel;

import java.util.List;
import java.util.function.Function;

@Mixin(MultiVariant.class)
public abstract class MultiVariantMixin {

    /**
     * @author TheGreatWolf
     * @reason Use faster, specialized models.
     */
    @javax.annotation.Nullable
    @Overwrite
    public @Nullable BakedModel bake(ModelBakery bakery,
                                     Function<Material, TextureAtlasSprite> spriteGetter,
                                     ModelState transform,
                                     ResourceLocation location) {
        List<Variant> variants = this.getVariants();
        if (variants.isEmpty()) {
            return null;
        }
        int size = variants.size();
        Variant variant0 = variants.get(0);
        if (size == 1) {
            return bakery.bake(variant0.getModelLocation(), variant0, spriteGetter);
        }
        int lastWeight = variant0.getWeight();
        boolean simple = true;
        for (int i = 1; i < size; ++i) {
            if (lastWeight != variants.get(i).getWeight()) {
                simple = false;
                break;
            }
        }
        if (simple) {
            SimpleWeightedModel.Builder builder = new SimpleWeightedModel.Builder();
            for (int i = 0; i < size; ++i) {
                Variant variant = variants.get(i);
                BakedModel baked = bakery.bake(variant.getModelLocation(), variant, spriteGetter);
                builder.add(baked);
            }
            return builder.build();
        }
        ComplexWeightedModel.Builder builder = new ComplexWeightedModel.Builder();
        for (int i = 0, len = variants.size(); i < len; i++) {
            Variant variant = variants.get(i);
            BakedModel bakedmodel = bakery.bake(variant.getModelLocation(), variant, spriteGetter);
            builder.add(bakedmodel, variant.getWeight());
        }
        return builder.build();
    }

    @Shadow
    public abstract List<Variant> getVariants();
}
