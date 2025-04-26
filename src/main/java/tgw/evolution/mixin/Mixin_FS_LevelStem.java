package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.sets.OSet;

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
                LevelStem value = (LevelStem) registry.getIteration(it);
                writableRegistry.register(key, value, registry.lifecycle(value));
            }
        }
        return writableRegistry;
    }
}
