package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Optional;
import java.util.Set;

@Mixin(LevelStem.class)
public abstract class Mixin_FS_LevelStem {

    @DeleteField @Shadow @Final private static Set<ResourceKey<LevelStem>> BUILTIN_ORDER;
    @Unique @RestoreFinal private static OSet<ResourceKey<LevelStem>> BUILTIN_ORDER_;
    @Mutable @Shadow @Final @RestoreFinal public static Codec<LevelStem> CODEC;
    @Mutable @Shadow @Final @RestoreFinal public static ResourceKey<LevelStem> END;
    @Mutable @Shadow @Final @RestoreFinal public static ResourceKey<LevelStem> NETHER;
    @Mutable @Shadow @Final @RestoreFinal public static ResourceKey<LevelStem> OVERWORLD;

    @Unique
    @ModifyStatic
    private static void clinit() {
        CODEC = RecordCodecBuilder.create(instance -> instance.group(DimensionType.CODEC.fieldOf("type").forGetter(LevelStem::typeHolder), ChunkGenerator.CODEC.fieldOf("generator").forGetter(LevelStem::generator)).apply(instance, instance.stable(LevelStem::new)));
        OVERWORLD = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("overworld"));
        NETHER = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_nether"));
        END = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, new ResourceLocation("the_end"));
        BUILTIN_ORDER_ = OSet.of(OVERWORLD, NETHER, END);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static Registry<LevelStem> sortMap(Registry<LevelStem> registry) {
        WritableRegistry<LevelStem> writableRegistry = new MappedRegistry<>(Registry.LEVEL_STEM_REGISTRY, Lifecycle.experimental(), null);
        OSet<ResourceKey<LevelStem>> builtinOrder = BUILTIN_ORDER_;
        for (long it = builtinOrder.beginIteration(); builtinOrder.hasNextIteration(it); it = builtinOrder.nextEntry(it)) {
            ResourceKey<LevelStem> key = builtinOrder.getIteration(it);
            LevelStem levelStem = registry.get(key);
            if (levelStem != null) {
                writableRegistry.register(key, levelStem, registry.lifecycle(levelStem));
            }
        }
        for (long it = registry.beginIteration(); registry.hasNextIteration(it); it = registry.nextEntry(it)) {
            ResourceKey<LevelStem> key = registry.getIterationKey(it);
            if (!BUILTIN_ORDER_.contains(key)) {
                LevelStem value = registry.getIteration(it);
                writableRegistry.register(key, value, registry.lifecycle(value));
            }
        }
        return writableRegistry;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static boolean stable(long l, Registry<LevelStem> registry) {
        if (registry.size() != BUILTIN_ORDER_.size()) {
            return false;
        }
        Optional<LevelStem> optional = registry.getOptional(OVERWORLD);
        Optional<LevelStem> optional2 = registry.getOptional(NETHER);
        Optional<LevelStem> optional3 = registry.getOptional(END);
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return false;
        }
        if (!optional.get().typeHolder().is(DimensionType.OVERWORLD_LOCATION) && !optional.get().typeHolder().is(DimensionType.OVERWORLD_CAVES_LOCATION)) {
            return false;
        }
        if (!optional2.get().typeHolder().is(DimensionType.NETHER_LOCATION)) {
            return false;
        }
        if (!optional3.get().typeHolder().is(DimensionType.END_LOCATION)) {
            return false;
        }
        if (!(optional2.get().generator() instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator) || !(optional3.get().generator() instanceof NoiseBasedChunkGenerator noiseBasedChunkGenerator2)) {
            return false;
        }
        if (!noiseBasedChunkGenerator.stable(l, NoiseGeneratorSettings.NETHER)) {
            return false;
        }
        if (!noiseBasedChunkGenerator2.stable(l, NoiseGeneratorSettings.END)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator.getBiomeSource() instanceof MultiNoiseBiomeSource multiNoiseBiomeSource)) {
            return false;
        }
        if (!multiNoiseBiomeSource.stable(MultiNoiseBiomeSource.Preset.NETHER)) {
            return false;
        }
        BiomeSource biomeSource = optional.get().generator().getBiomeSource();
        if (biomeSource instanceof MultiNoiseBiomeSource && !((MultiNoiseBiomeSource) biomeSource).stable(MultiNoiseBiomeSource.Preset.OVERWORLD)) {
            return false;
        }
        if (!(noiseBasedChunkGenerator2.getBiomeSource() instanceof TheEndBiomeSource theEndBiomeSource)) {
            return false;
        }
        return theEndBiomeSource.stable(l);
    }
}
