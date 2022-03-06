package tgw.evolution.client.models;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.ModelBakeEvent;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.item.modular.BakedModelModularTool;
import tgw.evolution.client.models.item.part.*;
import tgw.evolution.client.models.tile.BakedModelFirewoodPile;
import tgw.evolution.client.models.tile.BakedModelKnapping;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.RockVariant;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class ModelRegistry {

    private ModelRegistry() {
    }

    public static void register(ModelBakeEvent event) {
        for (RockVariant variant : RockVariant.VALUES) {
            Block block;
            try {
                block = variant.getKnapping();
            }
            catch (IllegalStateException e) {
                continue;
            }
            registerModel(event, block, variant, BakedModelKnapping::new);
        }
        registerModel(event, EvolutionBlocks.FIREWOOD_PILE.get(), BakedModelFirewoodPile::new);
        registerModel(event, EvolutionItems.MODULAR_TOOL.get(), BakedModelModularTool::new);
        registerModel(event, EvolutionItems.BLADE_PART.get(), BakedModelPartBlade::new);
        registerModel(event, EvolutionItems.GUARD_PART.get(), BakedModelPartGuard::new);
        registerModel(event, EvolutionItems.HALFHEAD_PART.get(), BakedModelPartHalfHead::new);
        registerModel(event, EvolutionItems.HANDLE_PART.get(), BakedModelPartHandle::new);
        registerModel(event, EvolutionItems.HEAD_PART.get(), BakedModelPartHead::new);
        registerModel(event, EvolutionItems.HILT_PART.get(), BakedModelPartHilt::new);
        registerModel(event, EvolutionItems.POLE_PART.get(), BakedModelPartPole::new);
        registerModel(event, EvolutionItems.POMMEL_PART.get(), BakedModelPartPommel::new);
    }

    private static void registerModel(ModelBakeEvent event, Item item, Function<BakedModel, BakedModel> newModel) {
        ModelResourceLocation modelResLoc = new ModelResourceLocation(item.getRegistryName(), "inventory");
        BakedModel oldModel = event.getModelManager().getModel(modelResLoc);
        event.getModelRegistry().put(modelResLoc, newModel.apply(oldModel));
    }

    private static void registerModel(ModelBakeEvent event, Block block, Function<BakedModel, BakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
            BakedModel existingModel = event.getModelRegistry().get(modelResLoc);
            if (existingModel == null) {
                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel));
            }
        }
    }

    private static <T> void registerModel(ModelBakeEvent event, Block block, T t, BiFunction<BakedModel, T, BakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShaper.stateToModelLocation(state);
            BakedModel existingModel = event.getModelRegistry().get(modelResLoc);
            if (existingModel == null) {
                Evolution.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel, t));
            }
        }
    }
}
