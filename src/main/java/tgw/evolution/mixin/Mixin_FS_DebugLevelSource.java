package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.ModifyStatic;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.util.List;
import java.util.Optional;

@Mixin(DebugLevelSource.class)
public abstract class Mixin_FS_DebugLevelSource extends ChunkGenerator {

    @Mutable @Shadow @Final @RestoreFinal public static Codec<DebugLevelSource> CODEC;
    @Mutable @Shadow @Final @RestoreFinal protected static BlockState AIR;
    @Mutable @Shadow @Final @RestoreFinal protected static BlockState BARRIER;
    @Mutable @Shadow @Final @RestoreFinal private static List<BlockState> ALL_BLOCKS;
    @Mutable @Shadow @Final @RestoreFinal private static int GRID_WIDTH;
    @Mutable @Shadow @Final @RestoreFinal private static int GRID_HEIGHT;

    public Mixin_FS_DebugLevelSource(Registry<StructureSet> registry, Optional<HolderSet<StructureSet>> optional, BiomeSource biomeSource) {
        super(registry, optional, biomeSource);
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        CODEC = RecordCodecBuilder.create(instance -> commonCodec(instance).and(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(debugLevelSource -> debugLevelSource.biomes)).apply(instance, instance.stable(DebugLevelSource::new)));
        OList<BlockState> states = new OArrayList<>();
        for (Block block : Registry.BLOCK) {
            OList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                states.add(possibleStates.get(i));
            }
        }
        states.trim();
        ALL_BLOCKS = states;
        GRID_WIDTH = Mth.ceil(Mth.sqrt(ALL_BLOCKS.size()));
        GRID_HEIGHT = Mth.ceil((float) ALL_BLOCKS.size() / GRID_WIDTH);
        AIR = Blocks.AIR.defaultBlockState();
        BARRIER = Blocks.BARRIER.defaultBlockState();
    }
}
