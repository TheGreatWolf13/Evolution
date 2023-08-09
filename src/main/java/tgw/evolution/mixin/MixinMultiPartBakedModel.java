package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.FastRandom;
import tgw.evolution.util.math.IRandom;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

@Mixin(MultiPartBakedModel.class)
public abstract class MixinMultiPartBakedModel implements BakedModel {

    @Shadow @Final private Map<BlockState, BitSet> selectorCache;

    @Shadow @Final private List<Pair<Predicate<BlockState>, BakedModel>> selectors;

    @Override
    @Overwrite
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        Evolution.deprecatedMethod();
        return List.of();
    }

    @Override
    public OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        if (state == null) {
            return OList.emptyList();
        }
        BitSet bitSet = this.selectorCache.get(state);
        if (bitSet == null) {
            bitSet = new BitSet();
            for (int i = 0; i < this.selectors.size(); ++i) {
                Pair<Predicate<BlockState>, BakedModel> pair = this.selectors.get(i);
                if (pair.getLeft().test(state)) {
                    bitSet.set(i);
                }
            }
            this.selectorCache.put(state, bitSet);
        }
        OList<BakedQuad> list = new OArrayList<>();
        IRandom random = new FastRandom();
        long seed = rand.nextLong();
        for (int i = 0, len = bitSet.length(); i < len; ++i) {
            if (bitSet.get(i)) {
                random.setSeed(seed);
                list.addAll(this.selectors.get(i).getRight().getQuads(state, side, random, IModelData.EMPTY));
            }
        }
        return list;
    }
}
