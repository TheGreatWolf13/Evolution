package tgw.evolution.world.feature;

import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import tgw.evolution.Evolution;

public final class EvolutionFeatures {

    public static final DeferredRegister<? extends Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Evolution.MODID);

    //Features
//    public static final RegistryObject<Feature<NoFeatureConfig>> TREE_BIG_OAK = FEATURES.register("tree_big_oak",
//                                                                                                  () -> new BigTreeFeature
//                                                                                                  (NoFeatureConfig::deserialize,
//                                                                                                                           false));
//    public static final RegistryObject<Feature<NoFeatureConfig>> ROCK = FEATURES.register("rock",
//                                                                                          () -> new FeatureRock(NoFeatureConfig::deserialize));
//    public static final RegistryObject<Feature<NoFeatureConfig>> STRATA = FEATURES.register("strata",
//                                                                                            () -> new FeatureStrata(NoFeatureConfig::deserialize));
//    public static final RegistryObject<Feature<SphereReplaceConfig>> SEDIMENTARY_DISKS = FEATURES.register("sedimentary_disks",
//                                                                                                           () -> new FeatureSedimentaryDisks(
//                                                                                                                   SphereReplaceConfig::deserialize));
    //Structures
//    public static final RegistryObject<Structure<NoFeatureConfig>> STRUCTURE_TEST = FEATURES.register("structure_test",
//                                                                                                       () -> new StructureTest
//                                                                                                       (NoFeatureConfig::deserialize));
//    public static final RegistryObject<Structure<NoFeatureConfig>> STRUCTURE_CAVE = FEATURES.register("structure_cave",
//                                                                                                      () -> new StructureCave
//                                                                                                      (NoFeatureConfig::deserialize));

    private EvolutionFeatures() {
    }

    public static void register() {
        FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

//    public static final IStructurePieceType PIECE_TEST = IStructurePieceType.register(StructureTestPieces.Piece::new, "evolution:piece_test");

//    public static final IStructurePieceType PIECE_CAVE = IStructurePieceType.register(StructureCavePieces.Piece::new, "evolution:piece_cave");

}
