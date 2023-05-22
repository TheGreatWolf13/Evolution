package tgw.evolution.mixin;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
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

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;loadingStack:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(ModelBakery instance, Set<ResourceLocation> value) {
        this.loadingStack = new ObjectOpenHashSet<>();
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;unbakedCache:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit0(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.unbakedCache = new Object2ReferenceOpenHashMap<>();
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakedCache:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit1(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.bakedCache = new Object2ReferenceOpenHashMap<>();
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;topLevelModels:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit2(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.topLevelModels = new Object2ReferenceOpenHashMap<>();
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "FIELD", target = "Lnet/minecraft/client/resources/model/ModelBakery;bakedTopLevelModels:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit3(ModelBakery instance, Map<ResourceLocation, UnbakedModel> value) {
        this.bakedTopLevelModels = new Object2ReferenceOpenHashMap<>();
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"), require = 4)
    private @Nullable HashMap onInitMap() {
        return null;
    }

    @Redirect(method = "<init>(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/client/color/block/BlockColors;Z)V", at =
    @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"), require = 1)
    private @Nullable HashSet onInitSet() {
        return null;
    }
}
