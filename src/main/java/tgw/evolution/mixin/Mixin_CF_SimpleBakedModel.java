package tgw.evolution.mixin;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.client.models.data.IModelData;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchSimpleBakedModel;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.math.IRandom;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(SimpleBakedModel.class)
public abstract class Mixin_CF_SimpleBakedModel implements BakedModel, PatchSimpleBakedModel {

    @Shadow @Final @DeleteField protected Map<Direction, List<BakedQuad>> culledFaces;
    @Mutable @Shadow @Final @RestoreFinal protected boolean hasAmbientOcclusion;
    @Mutable @Shadow @Final @RestoreFinal protected boolean isGui3d;
    @Mutable @Shadow @Final @RestoreFinal protected ItemOverrides overrides;
    @Mutable @Shadow @Final @RestoreFinal protected TextureAtlasSprite particleIcon;
    @Mutable @Shadow @Final @RestoreFinal protected ItemTransforms transforms;
    @Shadow @Final @DeleteField protected List<BakedQuad> unculledFaces;
    @Mutable @Shadow @Final @RestoreFinal protected boolean usesBlockLight;
    @Unique private Map<Direction, OList<BakedQuad>> culledFaces_;
    @Unique private OList<BakedQuad> unculledFaces_;

    @ModifyConstructor
    public Mixin_CF_SimpleBakedModel(List<BakedQuad> list, Map<Direction, List<BakedQuad>> map, boolean bl, boolean bl2, boolean bl3, TextureAtlasSprite textureAtlasSprite, ItemTransforms itemTransforms, ItemOverrides itemOverrides) {
        this.hasAmbientOcclusion = bl;
        this.isGui3d = bl3;
        this.usesBlockLight = bl2;
        this.particleIcon = textureAtlasSprite;
        this.transforms = itemTransforms;
        this.overrides = itemOverrides;
    }

    @Override
    @Overwrite
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
        Evolution.deprecatedMethod();
        return List.of();
    }

    @Override
    public OList<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, IRandom rand, IModelData extraData) {
        return side == null ? this.unculledFaces_ : this.culledFaces_.get(side);
    }

    @Override
    public void set(OList<BakedQuad> unculled, Map<Direction, OList<BakedQuad>> culled) {
        this.unculledFaces_ = unculled;
        this.culledFaces_ = culled;
    }
}
