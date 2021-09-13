//package tgw.evolution.init;
//
//import net.minecraftforge.event.RegistryEvent;
//import net.minecraftforge.registries.IForgeRegistry;
//import net.minecraftforge.registries.RegistryBuilder;
//import tgw.evolution.Evolution;
//import tgw.evolution.world.puzzle.pieces.IPuzzleDeserializer;
//
//public final class EvolutionRegistries {
//
//    public static IForgeRegistry<IPuzzleDeserializer<?>> structurePoolElement;
//
//    private EvolutionRegistries() {
//    }
//
//    public static void onNewRegistry(RegistryEvent.NewRegistry event) {
//        RegistryBuilder<IPuzzleDeserializer<?>> builder = new RegistryBuilder();
//        builder.setName(Evolution.getResource("puzzle"));
//        structurePoolElement = builder.create();
//    }
//}
