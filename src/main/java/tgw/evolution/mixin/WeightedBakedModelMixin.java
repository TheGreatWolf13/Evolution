package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Mixin(WeightedBakedModel.class)
public abstract class WeightedBakedModelMixin {

    @Shadow
    @Final
    private List<WeightedEntry.Wrapper<BakedModel>> list;

    @Shadow
    @Final
    private int totalWeight;

    @Nullable
    private static <T extends WeightedEntry> T getWeightedItem(List<T> entries, int weightedIndex) {
        for (int i = 0, l = entries.size(); i < l; i++) {
            T t = entries.get(i);
            weightedIndex -= t.getWeight().asInt();
            if (weightedIndex < 0) {
                return t;
            }
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations.
     */
    @Overwrite
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData modelData) {
        WeightedEntry.Wrapper<BakedModel> entry = getWeightedItem(this.list, Math.abs((int) rand.nextLong()) % this.totalWeight);
        if (entry != null) {
            return entry.getData().getQuads(state, side, rand, modelData);
        }
        return Collections.emptyList();
    }
}
