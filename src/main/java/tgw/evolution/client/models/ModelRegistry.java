package tgw.evolution.client.models;

public final class ModelRegistry {

    private ModelRegistry() {
    }

//    public static void register(ModelBakeEvent event) {
//        for (RockVariant variant : RockVariant.VALUES_STONE) {
//            Block block = variant.get(EvolutionBlocks.KNAPPING_BLOCKS);
//            registerModel(event, block, variant, BakedModelKnapping::new);
//        }
//        registerModel(event, EvolutionBlocks.FIREWOOD_PILE, BakedModelFirewoodPile::new);
////        registerModel(event, EvolutionItems.MODULAR_TOOL, BakedModelModularTool::new);
////        registerModel(event, EvolutionItems.PART_BLADE, BakedModelPartBlade::new);
////        registerModel(event, EvolutionItems.PART_GUARD, BakedModelPartGuard::new);
////        registerModel(event, EvolutionItems.PART_HALFHEAD, BakedModelPartHalfHead::new);
////        registerModel(event, EvolutionItems.PART_HANDLE, BakedModelPartHandle::new);
////        registerModel(event, EvolutionItems.PART_HEAD, BakedModelPartHead::new);
////        registerModel(event, EvolutionItems.PART_GRIP, BakedModelPartHilt::new);
////        registerModel(event, EvolutionItems.PART_POLE, BakedModelPartPole::new);
////        registerModel(event, EvolutionItems.PART_POMMEL, BakedModelPartPommel::new);
//    }

//    private static void registerModel(ModelBakeEvent event, Item item, Function<BakedModel, BakedModel> newModel) {
//        ModelResourceLocation modelResLoc = new ModelResourceLocation(item.getRegistryName(), "inventory");
//        BakedModel oldModel = event.getModelManager().getModel(modelResLoc);
//        event.getModelRegistry().put(modelResLoc, newModel.apply(oldModel));
//    }

//    private static void registerModel(ModelBakeEvent event, Block block, Function<BakedModel, BakedModel> newModel) {
//        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
//            //noinspection ObjectAllocationInLoop
//            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
//            BakedModel existingModel = event.getModelRegistry().get(modelResLoc);
//            if (existingModel == null) {
//                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
//            }
//            else {
//                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel));
//            }
//        }
//    }

//    private static <T> void registerModel(ModelBakeEvent event, Block block, T t, BiFunction<BakedModel, T, BakedModel> newModel) {
//        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
//            //noinspection ObjectAllocationInLoop
//            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
//            BakedModel existingModel = event.getModelRegistry().get(modelResLoc);
//            if (existingModel == null) {
//                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
//            }
//            else {
//                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel, t));
//            }
//        }
//    }
}
