package tgw.evolution.world.feature;

import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.SphereReplaceConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;
import tgw.evolution.blocks.BlockLog;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.world.feature.structures.StructureCave;
import tgw.evolution.world.feature.structures.StructureCavePieces;
import tgw.evolution.world.feature.structures.StructureTest;
import tgw.evolution.world.feature.structures.StructureTestPieces;
import tgw.evolution.world.feature.tree.AspenTreeFeature;
import tgw.evolution.world.feature.tree.BigTreeFeature;
import tgw.evolution.world.feature.tree.TreeFeature;

public class EvolutionFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = new DeferredRegister<>(ForgeRegistries.FEATURES, Evolution.MODID);

    //Features
    public static final RegistryObject<Feature<NoFeatureConfig>> TREE_BIRCH = FEATURES.register("tree_birch", () -> new AspenTreeFeature(NoFeatureConfig::deserialize, false, EvolutionBlocks.LOG_BIRCH.get().getDefaultState().with(BlockLog.TREE, true), EvolutionBlocks.LEAVES_BIRCH.get().getDefaultState(), 2));
    public static final RegistryObject<Feature<NoFeatureConfig>> TREE_BIG_OAK = FEATURES.register("tree_big_oak", () -> new BigTreeFeature(NoFeatureConfig::deserialize, false));
    public static final RegistryObject<Feature<NoFeatureConfig>> TREE_OAK = FEATURES.register("tree_oak", () -> new TreeFeature(NoFeatureConfig::deserialize, false));
    public static final RegistryObject<Feature<NoFeatureConfig>> ROCK = FEATURES.register("rock", () -> new FeatureRock(NoFeatureConfig::deserialize));
    public static final RegistryObject<Feature<NoFeatureConfig>> STRATA = FEATURES.register("strata", () -> new FeatureStrata(NoFeatureConfig::deserialize));
    public static final RegistryObject<Feature<SphereReplaceConfig>> SEDIMENTARY_DISKS = FEATURES.register("sedimentary_disks", () -> new FeatureSedimentaryDisks(SphereReplaceConfig::deserialize));
    //Structures
    public static final RegistryObject<Structure<NoFeatureConfig>> STRUCTURE_TEST = FEATURES.register("structure_test", () -> new StructureTest(NoFeatureConfig::deserialize));
    public static final RegistryObject<Structure<NoFeatureConfig>> STRUCTURE_CAVE = FEATURES.register("structure_cave", () -> new StructureCave(NoFeatureConfig::deserialize));

    public static final IStructurePieceType PIECE_TEST = IStructurePieceType.register(StructureTestPieces.Piece::new, "evolution:piece_test");
    public static final IStructurePieceType PIECE_CAVE = IStructurePieceType.register(StructureCavePieces.Piece::new, "evolution:piece_cave");

    public static void register() {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
