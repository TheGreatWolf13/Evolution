package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.Map;

@Mixin(BlockModelShaper.class)
public abstract class Mixin_CF_BlockModelShaper {

    @DeleteField @Shadow @Final private Map<BlockState, BakedModel> modelByStateCache;
    @Unique private final R2OMap<BlockState, BakedModel> modelByStateCache_;
    @Mutable @Shadow @Final @RestoreFinal private ModelManager modelManager;

    @ModifyConstructor
    public Mixin_CF_BlockModelShaper(ModelManager modelManager) {
        this.modelByStateCache_ = new R2OHashMap<>();
        this.modelManager = modelManager;
    }

    @Contract(value = "_ -> _")
    @Shadow
    public static ModelResourceLocation stateToModelLocation(BlockState blockState) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BakedModel getBlockModel(BlockState state) {
        BakedModel bakedModel = this.modelByStateCache_.get(state);
        if (bakedModel == null) {
            return this.modelManager.getMissingModel();
        }
        return bakedModel;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public void rebuildCache() {
        this.modelByStateCache_.clear();
        for (long it = Registry.BLOCK.beginIteration(); Registry.BLOCK.hasNextIteration(it); it = Registry.BLOCK.nextEntry(it)) {
            Block block = (Block) Registry.BLOCK.getIteration(it);
            OList<BlockState> possibleStates = block.getStateDefinition().getPossibleStates_();
            for (int i = 0, len = possibleStates.size(); i < len; ++i) {
                BlockState state = possibleStates.get(i);
                this.modelByStateCache_.put(state, this.modelManager.getModel(stateToModelLocation(state)));
            }
        }
    }
}
