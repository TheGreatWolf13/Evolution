package tgw.evolution.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.function.Function;
import java.util.stream.Stream;

@Mixin(PaintingTextureManager.class)
public abstract class MixinPaintingTextureManager extends TextureAtlasHolder {

    @Shadow @Final private static ResourceLocation BACK_SPRITE_LOCATION;

    public MixinPaintingTextureManager(TextureManager textureManager, ResourceLocation resourceLocation, String string) {
        super(textureManager, resourceLocation, string);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @Override
    public Stream<ResourceLocation> getResourcesToLoad() {
        Evolution.deprecatedMethod();
        return Stream.empty();
    }

    @Override
    public OSet<ResourceLocation> getResourcesToLoad_(Function<ResourceLocation, ResourceLocation> mapping) {
        OSet<ResourceLocation> set = new OHashSet<>();
        for (long it = Registry.MOTIVE.beginIteration(); Registry.MOTIVE.hasNextIteration(it); it = Registry.MOTIVE.nextEntry(it)) {
            set.add(mapping.apply(Registry.MOTIVE.getIterationLocation(it)));
        }
        set.add(mapping.apply(BACK_SPRITE_LOCATION));
        return set.immutable();
    }
}
