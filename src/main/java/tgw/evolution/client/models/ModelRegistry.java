package tgw.evolution.client.models;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.tile.BakedModelFirewoodPile;
import tgw.evolution.client.models.tile.BakedModelKnapping;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.util.RockVariant;

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
    }

    private static void registerModel(ModelBakeEvent event, Block block, Function<IBakedModel, IBakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShapes.stateToModelLocation(state);
            IBakedModel existingModel = event.getModelRegistry().get(modelResLoc);
            if (existingModel == null) {
                Evolution.LOGGER.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel));
            }
        }
    }

    private static <T> void registerModel(ModelBakeEvent event, Block block, T t, BiFunction<IBakedModel, T, IBakedModel> newModel) {
        for (BlockState state : block.getStateDefinition().getPossibleStates()) {
            //noinspection ObjectAllocationInLoop
            ModelResourceLocation modelResLoc = BlockModelShapes.stateToModelLocation(state);
            IBakedModel existingModel = event.getModelRegistry().get(modelResLoc);
            if (existingModel == null) {
                Evolution.LOGGER.warn("Did not find the expected vanilla baked model(s) for {} in registry", block);
            }
            else {
                event.getModelRegistry().put(modelResLoc, newModel.apply(existingModel, t));
            }
        }
    }
}
