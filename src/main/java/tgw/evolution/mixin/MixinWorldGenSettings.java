package tgw.evolution.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(WorldGenSettings.class)
public abstract class MixinWorldGenSettings {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static Registry<LevelStem> withOverworld(Registry<LevelStem> registry, Holder<DimensionType> holder, ChunkGenerator generator) {
        MappedRegistry<LevelStem> levelStems = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        levelStems.register(LevelStem.OVERWORLD, new LevelStem(holder, generator), Lifecycle.stable());
        for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
            ResourceKey<LevelStem> resourceKey = registry.getIterationKey(it);
            if (resourceKey == LevelStem.OVERWORLD) {
                continue;
            }
            LevelStem stem = registry.getIteration(it);
            levelStems.register(resourceKey, stem, registry.lifecycle(stem));
        }
        return levelStems;
    }
}
