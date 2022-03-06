package tgw.evolution.mixin;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.commons.lang3.tuple.Triple;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Mutable
    @Shadow
    @Final
    private Map<Triple<ResourceLocation, Transformation, Boolean>, BakedModel> bakedCache;
    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, BakedModel> bakedTopLevelModels;
    @Mutable
    @Shadow
    @Final
    private Set<ResourceLocation> loadingStack;
    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> topLevelModels;
    @Mutable
    @Shadow
    @Final
    private Map<ResourceLocation, UnbakedModel> unbakedCache;

    @Inject(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at = @At(
            "TAIL"))
    public void onConstructor(ResourceManager resourceManager, BlockColors blockColors, boolean vanillaBakery, CallbackInfo ci) {
        this.loadingStack = new ObjectOpenHashSet<>();
        this.unbakedCache = new Object2ReferenceOpenHashMap<>();
        this.bakedCache = new Object2ReferenceOpenHashMap<>();
        this.topLevelModels = new Object2ReferenceOpenHashMap<>();
        this.bakedTopLevelModels = new Object2ReferenceOpenHashMap<>();
    }
}
