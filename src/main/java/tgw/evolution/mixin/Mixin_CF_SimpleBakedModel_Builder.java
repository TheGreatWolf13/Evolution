package tgw.evolution.mixin;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.DirectionUtil;

import java.util.List;
import java.util.Map;

@SuppressWarnings("ClassWithOnlyPrivateConstructors")
@Mixin(SimpleBakedModel.Builder.class)
public abstract class Mixin_CF_SimpleBakedModel_Builder {

    @Unique private final Map<Direction, OList<BakedQuad>> culledFaces_;
    @Unique private final OList<BakedQuad> unculledFaces_;
    @Shadow @Final @DeleteField private Map<Direction, List<BakedQuad>> culledFaces;
    @Mutable @Shadow @Final @RestoreFinal private boolean hasAmbientOcclusion;
    @Mutable @Shadow @Final @RestoreFinal private boolean isGui3d;
    @Mutable @Shadow @Final @RestoreFinal private ItemOverrides overrides;
    @Shadow private @Nullable TextureAtlasSprite particleIcon;
    @Mutable @Shadow @Final @RestoreFinal private ItemTransforms transforms;
    @Shadow @Final @DeleteField private List<BakedQuad> unculledFaces;
    @Mutable @Shadow @Final @RestoreFinal private boolean usesBlockLight;

    @ModifyConstructor
    private Mixin_CF_SimpleBakedModel_Builder(boolean ao, boolean blockLight, boolean gui3d, ItemTransforms transforms, ItemOverrides overrides) {
        this.unculledFaces_ = new OArrayList<>();
        this.culledFaces_ = Maps.newEnumMap(Direction.class);
        for (Direction direction : DirectionUtil.ALL) {
            //noinspection ObjectAllocationInLoop
            this.culledFaces_.put(direction, new OArrayList<>());
        }
        this.overrides = overrides;
        this.hasAmbientOcclusion = ao;
        this.usesBlockLight = blockLight;
        this.isGui3d = gui3d;
        this.transforms = transforms;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public SimpleBakedModel.Builder addCulledFace(Direction direction, BakedQuad bakedQuad) {
        this.culledFaces_.get(direction).add(bakedQuad);
        return (SimpleBakedModel.Builder) (Object) this;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public SimpleBakedModel.Builder addUnculledFace(BakedQuad bakedQuad) {
        this.unculledFaces_.add(bakedQuad);
        return (SimpleBakedModel.Builder) (Object) this;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public BakedModel build() {
        if (this.particleIcon == null) {
            throw new RuntimeException("Missing particle!");
        }
        //noinspection ConstantConditions
        SimpleBakedModel model = new SimpleBakedModel(null, null, this.hasAmbientOcclusion, this.usesBlockLight, this.isGui3d, this.particleIcon, this.transforms, this.overrides);
        OList<BakedQuad> unculled = this.unculledFaces_;
        Map<Direction, OList<BakedQuad>> map = this.culledFaces_;
        if (unculled.isEmpty()) {
            unculled = OList.emptyList();
        }
        else {
            unculled.trimCollection();
        }
        if (map.isEmpty()) {
            map = Map.of();
        }
        else {
            for (Direction direction : DirectionUtil.ALL) {
                OList<BakedQuad> list = this.culledFaces_.get(direction);
                if (list.isEmpty()) {
                    this.culledFaces_.put(direction, OList.emptyList());
                }
                else {
                    list.trimCollection();
                }
            }
        }
        model.set(unculled, map);
        return model;
    }
}
