package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.tile.BakedModelFirewoodPile;
import tgw.evolution.init.EvolutionBlocks;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class ModelRegistry {

    private ModelRegistry() {
    }

    public static void register(Map<ResourceLocation, BakedModel> models) {
//        for (RockVariant variant : RockVariant.VALUES_STONE) {
//            Block block = variant.get(EvolutionBlocks.KNAPPING_BLOCKS);
//            registerModel(event, block, variant, BakedModelKnapping::new);
//        }
        registerModel(models, EvolutionBlocks.FIREWOOD_PILE, BakedModelFirewoodPile::new);
//        registerModel(event, EvolutionItems.MODULAR_TOOL, BakedModelModularTool::new);
//        registerModel(event, EvolutionItems.PART_BLADE, BakedModelPartBlade::new);
//        registerModel(event, EvolutionItems.PART_GUARD, BakedModelPartGuard::new);
//        registerModel(event, EvolutionItems.PART_HALFHEAD, BakedModelPartHalfHead::new);
//        registerModel(event, EvolutionItems.PART_HANDLE, BakedModelPartHandle::new);
//        registerModel(event, EvolutionItems.PART_HEAD, BakedModelPartHead::new);
//        registerModel(event, EvolutionItems.PART_GRIP, BakedModelPartHilt::new);
//        registerModel(event, EvolutionItems.PART_POLE, BakedModelPartPole::new);
//        registerModel(event, EvolutionItems.PART_POMMEL, BakedModelPartPommel::new);
    }

    private static void registerModel(Map<ResourceLocation, BakedModel> models, Item item, Function<BakedModel, BakedModel> newModel) {
        ModelResourceLocation modelResLoc = new ModelResourceLocation(Registry.ITEM.getKey(item), "inventory");
        BakedModel oldModel = models.get(modelResLoc);
        models.put(modelResLoc, newModel.apply(oldModel));
    }

    private static void registerModel(Map<ResourceLocation, BakedModel> models, Block block, Function<BakedModel, BakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
            BakedModel existingModel = models.get(modelResLoc);
            if (existingModel == null) {
                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                models.put(modelResLoc, newModel.apply(existingModel));
            }
        }
    }

    private static <T> void registerModel(Map<ResourceLocation, BakedModel> models, Block block, T t, BiFunction<BakedModel, T, BakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
            BakedModel existingModel = models.get(modelResLoc);
            if (existingModel == null) {
                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                models.put(modelResLoc, newModel.apply(existingModel, t));
            }
        }
    }
}
