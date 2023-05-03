package tgw.evolution.mixin;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.ModelProvider;

import java.util.Map;

@Mixin(ModelBuilder.class)
public abstract class ModelBuilderMixin<T extends ModelBuilder<T>> extends ModelFile {

    @Shadow
    @Final
    protected ExistingFileHelper existingFileHelper;
    @Shadow
    @Final
    protected Map<String, String> textures;

    public ModelBuilderMixin(ResourceLocation location) {
        super(location);
    }

    @Shadow
    protected abstract T self();

    @Shadow
    public abstract T texture(String key, String texture);

    /**
     * @author TheGreatWolf
     * @reason No need to throw when the texture file doesn't exist, just warn me in the log.
     */
    @Overwrite
    public T texture(String key, ResourceLocation texture) {
        Preconditions.checkNotNull(key, "Key must not be null");
        Preconditions.checkNotNull(texture, "Texture must not be null");
        if (!this.existingFileHelper.exists(texture, ModelProvider.TEXTURE)) {
            Evolution.warn("Texture {} does not exist in any known resource pack", texture);
        }
        this.textures.put(key, texture.toString());
        return this.self();
    }
}
